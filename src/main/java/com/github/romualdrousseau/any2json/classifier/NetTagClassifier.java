package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.Signature;
import org.tensorflow.types.TFloat32;

import com.github.romualdrousseau.any2json.TagClassifier;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.commons.PythonManager;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.types.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class NetTagClassifier implements TagClassifier {

    public static final int IN_ENTITY_SIZE = 10;
    public static final int IN_NAME_SIZE = 10;
    public static final int IN_CONTEXT_SIZE = 100;
    public static final int OUT_TAG_SIZE = 64;

    public NetTagClassifier(final Model model, final List<String> vocabulary, final int ngrams, final int wordMinSize,
            final List<String> lexicon, final Path modelPath) {
        this.model = model;
        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.wordMinSize = wordMinSize;
        this.lexicon = lexicon;

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon, this.wordMinSize)
                : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);

        this.modelPath = modelPath;
        if (this.modelPath.toFile().exists()) {
            this.tagClassifierModel = SavedModelBundle.load(modelPath.toString(), "serve");
            this.tagClassifierFunc = this.tagClassifierModel.function(Signature.DEFAULT_KEY);
        } else {
            this.tagClassifierModel = null;
            this.tagClassifierFunc = null;
        }
        this.modelIsTemp = false;

        // Update the model with the classifier parameters

        this.model.toJSON().setArray("vocabulary", JSON.arrayOf(this.vocabulary));
        this.model.toJSON().setInt("ngram", this.ngrams);
        this.model.toJSON().setInt("wordMinSize", this.wordMinSize);
        this.model.toJSON().setArray("lexicon", JSON.arrayOf(this.lexicon));
        this.model.toJSON().setString("model", this.modelToJSONString(this.modelPath));
    }

    public NetTagClassifier(final Model model) {
        this.model = model;
        this.vocabulary = JSON.<String>streamOf(model.toJSON().getArray("vocabulary")).toList();
        this.ngrams = model.toJSON().getInt("ngrams");
        this.wordMinSize = model.toJSON().getInt("wordMinSize");
        this.lexicon = JSON.<String>streamOf(model.toJSON().getArray("lexicon")).toList();

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon, this.wordMinSize)
                : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);

        this.modelPath = this.JSONStringToModel(model.toJSON().getString("model"));
        if (modelPath.toFile().exists()) {
            this.tagClassifierModel = SavedModelBundle.load(modelPath.toString(), "serve");
            this.tagClassifierFunc = this.tagClassifierModel.function(Signature.DEFAULT_KEY);
        } else {
            this.tagClassifierModel = null;
            this.tagClassifierFunc = null;
        }
        this.modelIsTemp = true;
    }

    @Override
    public void close() throws Exception {
        if (this.modelIsTemp) {
            Disk.deleteDir(this.modelPath);
        }
        if (this.tagClassifierModel != null) {
            this.tagClassifierModel.close();
        }
    }

    @Override
    public String predict(final String name, final List<String> entities, final List<String> context) {
        return this.predict(this.buildPredictEntry(name, entities, context));
    }

    public List<String> getVocabulary() {
        return this.vocabulary;
    }

    public List<String> getLexicon() {
        return this.lexicon;
    }

    public TrainingEntry buildTrainingEntry(
            final String name, final List<String> entities, final List<String> context, final String label) {
        return new TrainingEntry(
                this.buildPredictEntry(name, entities, context),
                Text.pad_sequence(Text.to_categorical(label, this.model.getTagList()), OUT_TAG_SIZE));
    }

    public Process fit(final List<List<Integer>> trainingSet, final List<List<Integer>> validationSet)
            throws IOException, InterruptedException, URISyntaxException {

        final String dimensions = String.format("%d,%d,%d,%d", IN_ENTITY_SIZE, IN_NAME_SIZE, IN_CONTEXT_SIZE,
                OUT_TAG_SIZE);

        final Path trainPath = Files.createTempDirectory("any2json").toAbsolutePath();
        final JSONArray list1 = JSON.newArray();
        trainingSet.forEach(x -> {
            list1.append(JSON.arrayOf(x.toString()));
        });
        JSON.saveArray(list1, trainPath.resolve("training.json"));

        final JSONArray list2 = JSON.newArray();
        validationSet.forEach(x -> {
            list2.append(JSON.arrayOf(x.toString()));
        });
        JSON.saveArray(list2, trainPath.resolve("validation.json"));

        final Map<String, String> disableTFLog = Map.of(
                "TF_CPP_MIN_VLOG_LEVEL", "3",
                "TF_CPP_MIN_LOG_LEVEL", "3");

        return new PythonManager("kernels.tf")
                .setEnviroment(disableTFLog)
                .run("-V " + this.vocabulary.size(), "-s " + dimensions, "-t " + trainPath, "-m " + this.modelPath);
    }

    private List<Integer> buildPredictEntry(final String name, final List<String> entities,
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

    private String predict(final List<Integer> predictSet) {
        if (this.tagClassifierFunc == null) {
            return this.model.getTagList().get(0);
        }

        final double[] entityInput = predictSet.subList(0, IN_ENTITY_SIZE).stream().mapToDouble(x -> x).toArray();

        final double[] nameInput = predictSet.subList(IN_ENTITY_SIZE, IN_ENTITY_SIZE + IN_NAME_SIZE).stream()
                .mapToDouble(x -> x).toArray();

        final double[] contextInput = predictSet
                .subList(IN_ENTITY_SIZE + IN_NAME_SIZE, IN_ENTITY_SIZE + IN_NAME_SIZE + IN_CONTEXT_SIZE).stream()
                .mapToDouble(x -> x).toArray();

        final Map<String, org.tensorflow.Tensor> inputs = Map.of(
                "entity_input", Tensor.of(entityInput).reshape(1, -1).toTFloat32(),
                "name_input", Tensor.of(nameInput).reshape(1, -1).toTFloat32(),
                "context_input", Tensor.of(contextInput).reshape(1, -1).toTFloat32());

        final Tensor result = Tensor.of((TFloat32) this.tagClassifierFunc.call(inputs).get("tag_output").get());
        return this.model.getTagList().get((int) result.argmax(1).item(0));
    }

    private String modelToJSONString(final Path modelPath) {
        if (!modelPath.toFile().exists()) {
            return "";
        }
        try {
            final Path temp = Files.createTempFile("model-", ".zip");
            Disk.zipDir(modelPath, temp.toFile());
            return Base64.getEncoder().encodeToString(Files.readAllBytes(temp));
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    private Path JSONStringToModel(final String modelString) {
        try {
            final Path temp1 = Files.createTempFile("model-", ".zip");
            final Path modelPath = Files.createTempDirectory("model-");
            Files.write(temp1, Base64.getDecoder().decode(modelString), StandardOpenOption.CREATE);
            Disk.unzipDir(temp1, modelPath);
            return modelPath;
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    private final Model model;
    private final List<String> vocabulary;
    private final int ngrams;
    private final int wordMinSize;
    private final List<String> lexicon;
    private final Path modelPath;
    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;
    private final SavedModelBundle tagClassifierModel;
    private final SessionFunction tagClassifierFunc;
    private final boolean modelIsTemp;
}
