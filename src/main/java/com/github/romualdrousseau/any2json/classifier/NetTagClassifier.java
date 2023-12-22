package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.Signature;
import org.tensorflow.exceptions.TensorFlowException;
import org.tensorflow.types.TFloat32;

import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.any2json.util.TempFile;
import com.github.romualdrousseau.shuju.commons.PythonManager;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.types.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class NetTagClassifier extends SimpleTagClassifier {

    public static final int IN_ENTITY_SIZE = 10;
    public static final int IN_NAME_SIZE = 10;
    public static final int IN_CONTEXT_SIZE = 100;
    public static final int OUT_TAG_SIZE = 64;

    private final Model model;
    private final List<String> vocabulary;
    private final int ngrams;
    private final int wordMinSize;
    private final List<String> lexicon;
    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;
    private final boolean isModelTemp;

    private Path modelPath;
    private SavedModelBundle tagClassifierModel;
    private SessionFunction tagClassifierFunc;

    public NetTagClassifier(final Model model, final List<String> vocabulary, final int ngrams, final int wordMinSize,
            final List<String> lexicon, final Path modelPath) {
        super(model);

        this.model = model;
        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.wordMinSize = wordMinSize;
        this.lexicon = lexicon;
        this.tokenizer = (ngrams == 0) ? new ShingleTokenizer(this.lexicon, this.wordMinSize)
                : new NgramTokenizer(ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);
        this.modelPath = modelPath;
        this.isModelTemp = false;
        this.updateModel();
    }

    public NetTagClassifier(final Model model) {
        super(model);

        this.model = model;
        this.vocabulary = JSON.<String>streamOf(model.toJSON().getArray("vocabulary")).toList();
        this.ngrams = model.toJSON().getInt("ngrams");
        this.wordMinSize = model.toJSON().getInt("wordMinSize");
        this.lexicon = JSON.<String>streamOf(model.toJSON().getArray("lexicon")).toList();
        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon, this.wordMinSize)
                : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);
        this.modelPath = null;
        this.isModelTemp = true;
    }

    @Override
    public void close() throws Exception {
        if (tagClassifierModel != null) {
            tagClassifierModel.close();
            tagClassifierModel = null;
            tagClassifierFunc = null;
        }
        if (this.modelPath != null && this.isModelTemp) {
            Disk.deleteDir(modelPath);
        }
    }

    @Override
    public String predict(final String name, final List<String> entities, final List<String> context) {
        this.ensureClassifierLoaded();
        if (this.tagClassifierModel == null) {
            return HeaderTag.None.getValue();
        }

        final var vector = this.createTrainingVector(name, entities, context).stream().mapToDouble(x -> x).toArray();
        final var entityInput = Arrays.stream(vector, 0, IN_ENTITY_SIZE).toArray();
        final var nameInput = Arrays.stream(vector, IN_ENTITY_SIZE, IN_ENTITY_SIZE + IN_NAME_SIZE).toArray();
        final var contextInput = Arrays
                .stream(vector, IN_ENTITY_SIZE + IN_NAME_SIZE, IN_ENTITY_SIZE + IN_NAME_SIZE + IN_CONTEXT_SIZE)
                .toArray();
        final Map<String, org.tensorflow.Tensor> inputs = Map.of(
                "entity_input", Tensor.of(entityInput).reshape(1, -1).toTFloat32(),
                "name_input", Tensor.of(nameInput).reshape(1, -1).toTFloat32(),
                "context_input", Tensor.of(contextInput).reshape(1, -1).toTFloat32());

        final var result = Tensor.of((TFloat32) this.tagClassifierFunc.call(inputs).get("tag_output").get());
        return this.model.getTagList().get((int) result.argmax(1).item(0));
    }

    public Process fit(final List<TrainingEntry> trainingSet, final List<TrainingEntry> validationSet)
            throws IOException, InterruptedException, URISyntaxException {

        if (this.modelPath == null) {
            this.modelPath = this.JSONStringToModelPath(model.toJSON().getString("model"));
        }
        if (tagClassifierModel != null) {
            tagClassifierModel.close();
            tagClassifierModel = null;
            tagClassifierFunc = null;
        }

        final String dimensions = String.format("%d,%d,%d,%d", IN_ENTITY_SIZE, IN_NAME_SIZE, IN_CONTEXT_SIZE,
                OUT_TAG_SIZE);

        final Path trainPath = Files.createTempDirectory("any2json").toAbsolutePath();

        final JSONArray list1 = JSON.newArray();
        trainingSet.forEach(x -> list1.append(JSON.arrayOf(x.getVector().toString())));
        JSON.saveArray(list1, trainPath.resolve("training.json"));

        final JSONArray list2 = JSON.newArray();
        validationSet.forEach(x -> list2.append(JSON.arrayOf(x.getVector().toString())));
        JSON.saveArray(list2, trainPath.resolve("validation.json"));

        final Map<String, String> disableTFLog = Map.of(
                "TF_CPP_MIN_VLOG_LEVEL", "3",
                "TF_CPP_MIN_LOG_LEVEL", "3");

        return new PythonManager("kernels.tf")
                .setEnviroment(disableTFLog)
                .run("-V " + this.vocabulary.size(), "-s " + dimensions, "-t " + trainPath, "-m " + this.modelPath);
    }

    public List<String> getVocabulary() {
        return this.vocabulary;
    }

    public List<String> getLexicon() {
        return this.lexicon;
    }

    public TrainingEntry createTrainingEntry(
            final String name, final List<String> entities, final List<String> context, final String label) {
        return new TrainingEntry(
                this.createTrainingVector(name, entities, context),
                Text.pad_sequence(Text.to_categorical(label, this.model.getTagList()), OUT_TAG_SIZE));
    }

    public void updateModel() {
        this.model.toJSON().setArray("vocabulary", JSON.arrayOf(this.vocabulary));
        this.model.toJSON().setInt("ngrams", this.ngrams);
        this.model.toJSON().setInt("wordMinSize", this.wordMinSize);
        this.model.toJSON().setArray("lexicon", JSON.arrayOf(this.lexicon));
        this.model.toJSON().setString("model", this.modelToJSONString(this.modelPath));
    }

    private List<Integer> createTrainingVector(final String name, final List<String> entities,
            final List<String> context) {
        final List<Integer> part1 = Text.to_categorical(entities, this.model.getEntityList());
        final List<Integer> part2 = Text.one_hot(name, this.model.getFilters(), this.tokenizer, this.hasher);
        final List<Integer> part3 = context.stream()
                .filter(x -> !x.equals(name))
                .flatMap(x -> Text.one_hot(x, this.model.getFilters(), this.tokenizer, this.hasher).stream())
                .distinct().sorted().toList();
        return Stream.of(
                Text.pad_sequence(part1, IN_ENTITY_SIZE).subList(0, IN_ENTITY_SIZE),
                Text.pad_sequence(part2, IN_NAME_SIZE).subList(0, IN_NAME_SIZE),
                Text.pad_sequence(part3, IN_CONTEXT_SIZE).subList(0, IN_CONTEXT_SIZE))
                .flatMap(Collection::stream)
                .toList();
    }

    private void ensureClassifierLoaded() {
        if (this.modelPath == null) {
            this.modelPath = this.JSONStringToModelPath(model.toJSON().getString("model"));
        }
        try {
            if (this.tagClassifierModel == null) {
                this.tagClassifierModel = SavedModelBundle.load(modelPath.toString(), "serve");
                this.tagClassifierFunc = this.tagClassifierModel.function(Signature.DEFAULT_KEY);
            }
        } catch(TensorFlowException x) {
            if (tagClassifierModel != null) {
                tagClassifierModel.close();
                tagClassifierModel = null;
                tagClassifierFunc = null;
            }
        }
    }

    private String modelToJSONString(final Path modelPath) {
        if (modelPath == null || !modelPath.toFile().exists()) {
            return "";
        }
        try (final var temp = new TempFile("model-", ".zip")) {
            Disk.zipDir(modelPath, temp.getPath().toFile());
            return Base64.getEncoder().encodeToString(Files.readAllBytes(temp.getPath()));
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    private Path JSONStringToModelPath(final String modelString) {
        if (modelString == null) {
            return null;
        }
        try (final var temp = new TempFile("model-", ".zip")) {
            Files.write(temp.getPath(), Base64.getDecoder().decode(modelString), StandardOpenOption.CREATE);
            final var modelPath = Files.createTempDirectory("model-");
            Disk.unzipDir(temp.getPath(), modelPath);
            modelPath.toFile().deleteOnExit();
            return modelPath;
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
