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
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.Signature;
import org.tensorflow.exceptions.TensorFlowException;
import org.tensorflow.types.TFloat32;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TagClassifier;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.any2json.util.TempFile;
import com.github.romualdrousseau.shuju.types.Tensor;
import com.github.romualdrousseau.shuju.commons.PythonManager;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class NetTagClassifier extends SimpleTagClassifier implements Trainable {

    public static final int IN_ENTITY_SIZE = 10;
    public static final int IN_NAME_SIZE = 10;
    public static final int IN_CONTEXT_SIZE = 100;
    public static final int OUT_TAG_SIZE = 64;

    private final List<String> vocabulary;
    private final int ngrams;
    private final int wordMinSize;
    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;
    private final boolean isModelTemp;

    private Optional<Path> modelPath;
    private SavedModelBundle tagClassifierModel;
    private SessionFunction tagClassifierFunc;

    public NetTagClassifier(final List<String> vocabulary, final int ngrams, final int wordMinSize,
            final List<String> lexicon, final Optional<Path> modelPath) {
        this.setLexicon(lexicon);

        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.wordMinSize = wordMinSize;

        this.tokenizer = (ngrams == 0)
                ? new ShingleTokenizer(this.getLexicon(), this.wordMinSize)
                : new NgramTokenizer(ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);
        this.isModelTemp = modelPath.filter(x -> x.toFile().exists()).isEmpty();
        this.modelPath = modelPath;
    }

    public NetTagClassifier(final Model model, final TagClassifier.TagStyle tagStyle) {
        this(
                model.getData().getList("vocabulary"),
                model.getData().<Integer>get("ngrams").orElse(0),
                model.getData().<Integer>get("wordMinSize").orElse(2),
                model.getData().getList("lexicon"),
                Optional.ofNullable(model.getModelAttributes().get("modelPath")).map(Path::of));

        assert this.isModelTemp && model.getData().<String>get("model").isPresent() : "model element must exist";

        this.setModel(model);
        this.setTagStyle(tagStyle);
    }

    @Override
    public void close() throws Exception {
        this.closeModelML();
    }

    @Override
    public void updateModelData() {
        this.getModel().getData().setList("vocabulary", this.vocabulary);
        this.getModel().getData().set("ngrams", this.ngrams);
        this.getModel().getData().set("wordMinSize", this.wordMinSize);
        this.getModel().getData().setList("lexicon", this.getLexicon());
        if (!this.isModelTemp && this.modelPath.isPresent()) {
            this.getModel().getModelAttributes().put("modelPath", this.modelPath.get().toString());
            this.getModel().getData().set("model", this.serializeModelML(this.modelPath.get()));
        }
    }

    @Override
    public String predict(final Table table, final Header header) {
        final var name = header.getName();
        final var entities = StreamSupport.stream(header.entities().spliterator(), false).toList();
        final var context = StreamSupport.stream(table.getHeaderNames().spliterator(), false).toList();

        if (!this.loadModelML()) {
            return HeaderTag.None.getValue();
        }

        final var vector = this.getInputVector(name, entities, context).stream().mapToDouble(x -> x).toArray();
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
        return this.getModel().getTagList().get((int) result.argmax(1).item(0));
    }

    @Override
    public List<Integer> getInputVector(final String name, final List<String> entities,
            final List<String> context) {
        final var part1 = Text.to_categorical(entities, this.getModel().getEntityList());
        final var part2 = Text.one_hot(name, this.getModel().getFilterList(), this.tokenizer, this.hasher);
        final var part3 = context.stream()
                .filter(x -> !x.equals(name))
                .flatMap(x -> Text.one_hot(x, this.getModel().getFilterList(), this.tokenizer, this.hasher).stream())
                .distinct().sorted().toList();
        return Stream.of(
                Text.pad_sequence(part1, IN_ENTITY_SIZE).subList(0, IN_ENTITY_SIZE),
                Text.pad_sequence(part2, IN_NAME_SIZE).subList(0, IN_NAME_SIZE),
                Text.pad_sequence(part3, IN_CONTEXT_SIZE).subList(0, IN_CONTEXT_SIZE))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public List<Integer> getOutputVector(final String label) {
        return Text.pad_sequence(Text.to_categorical(label, this.getModel().getTagList()), OUT_TAG_SIZE);
    }

    public List<String> getVocabulary() {
        return this.vocabulary;
    }

    public Process fit(final List<TrainingEntry> trainingSet, final List<TrainingEntry> validationSet)
            throws IOException, InterruptedException, URISyntaxException {
        this.closeModelML();

        final var dimensions = String.format("%d,%d,%d,%d", IN_ENTITY_SIZE, IN_NAME_SIZE, IN_CONTEXT_SIZE,
                OUT_TAG_SIZE);

        final var trainPath = Files.createTempDirectory("any2json").toAbsolutePath();

        final var list1 = JSON.newArray();
        trainingSet.forEach(x -> list1.append(JSON.arrayOf(x.getVector().toString())));
        JSON.saveArray(list1, trainPath.resolve("training.json"));

        final var list2 = JSON.newArray();
        validationSet.forEach(x -> list2.append(JSON.arrayOf(x.getVector().toString())));
        JSON.saveArray(list2, trainPath.resolve("validation.json"));

        final var disableTFLog = Map.of("TF_CPP_MIN_VLOG_LEVEL", "3", "TF_CPP_MIN_LOG_LEVEL", "3");

        return new PythonManager("kernels.tf")
                .setEnviroment(disableTFLog)
                .run("-V " + this.vocabulary.size(),
                        "-s " + dimensions,
                        "-t " + trainPath,
                        "-m " + this.modelPath.get());
    }

    private boolean loadModelML() {
        try {
            if (this.modelPath.isEmpty()) {
                final var modelString = this.getModel().getData().<String>get("model").get();
                this.modelPath = Optional.of(this.unserializeModelML(modelString));
            }
            if (this.tagClassifierModel == null) {
                this.tagClassifierModel = SavedModelBundle.load(this.modelPath.get().toString(), "serve");
                this.tagClassifierFunc = this.tagClassifierModel.function(Signature.DEFAULT_KEY);
            }
            return this.tagClassifierModel != null;
        } catch (final TensorFlowException x) {
            this.closeModelML();
            return false;
        }
    }

    private void closeModelML() {
        try {
            if (tagClassifierModel != null) {
                tagClassifierModel.close();
                tagClassifierModel = null;
                tagClassifierFunc = null;
            }
            if (this.isModelTemp && this.modelPath.isPresent() && this.modelPath.get().toFile().exists()) {
                Disk.deleteDir(modelPath.get());
            }
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    private String serializeModelML(final Path modelPath) {
        assert modelPath != null && modelPath.toFile().exists();
        try (final var temp = new TempFile("model-", ".zip")) {
            Disk.zipDir(modelPath, temp.getPath().toFile());
            return Base64.getEncoder().encodeToString(Files.readAllBytes(temp.getPath()));
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    private Path unserializeModelML(final String modelString) {
        assert modelString != null && !modelString.isEmpty();
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
