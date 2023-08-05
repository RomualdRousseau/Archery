package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.Signature;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class LayexAndNetClassifier extends LayexClassifier implements ITagClassifier<List<Integer>> {

    private static final int IN_ENTITY_SIZE = 10;
    private static final int IN_NAME_SIZE = 10;
    private static final int IN_CONTEXT_SIZE = 100;
    private static final int OUT_TAG_SIZE = 64;

    private final List<String> vocabulary;
    private final int ngrams;
    private final List<String> lexicon;
    private final List<String> tags;
    private final List<String> requiredTags;
    private final Path modelPath;

    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;

    private final SavedModelBundle tagClassifierModel;
    private final SessionFunction tagClassifierFunc;
    private final boolean modelIsTemp;

    public LayexAndNetClassifier(final List<String> vocabulary, final int ngrams, final List<String> lexicon,
            final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> tags, final List<String> requiredTags, final List<String> pivotEntityList,
            final List<String> metaLayexes, final List<String> dataLayexes, final Path modelPath) {
        super(entities, patterns, filters, pivotEntityList, metaLayexes, dataLayexes);

        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.lexicon = lexicon;
        this.tags = tags;
        this.requiredTags = requiredTags;

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon) : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);

        this.modelPath = modelPath;
        if (modelPath.toFile().exists()) {
            this.tagClassifierModel = SavedModelBundle.load(modelPath.toString(), "serve");
            this.tagClassifierFunc = this.tagClassifierModel.function(Signature.DEFAULT_KEY);
        } else {
            this.tagClassifierModel = null;
            this.tagClassifierFunc = null;
        }
        this.modelIsTemp = false;
    }

    public LayexAndNetClassifier(final JSONObject json) {
        super(json);

        this.vocabulary = JSON.<String>Stream(json.getJSONArray("vocabulary")).toList();
        this.ngrams = json.getInt("ngrams");
        this.lexicon = JSON.<String>Stream(json.getJSONArray("lexicon")).toList();
        this.tags = JSON.<String>Stream(json.getJSONArray("tags")).toList();
        this.requiredTags = JSON.<String>Stream(json.getJSONArray("requiredTags")).toList();

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon) : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);

        this.modelPath = this.JSONStringToModel(json.getString("model"));
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
    public List<String> getTagList() {
        return this.tags;
    }

    @Override
    public List<String> getRequiredTagList() {
        return this.requiredTags;
    }

    @Override
    public List<Integer> buildPredictSet(final String name, final List<String> entities, final List<String> context) {
        final List<Integer> part1 = Text.to_categorical(entities, this.entities);
        final List<Integer> part2 = Text.one_hot(name, this.filters, this.tokenizer, this.hasher);
        final List<Integer> part3 = context.stream()
                .filter(x -> !x.equals(name))
                .flatMap(x -> Text.one_hot(x, this.filters, this.tokenizer, this.hasher).stream())
                .distinct().sorted().toList();
        return Stream.concat(Stream.concat(
                Text.pad_sequence(part1, IN_ENTITY_SIZE).stream().limit(IN_ENTITY_SIZE),
                Text.pad_sequence(part2, IN_NAME_SIZE).stream().limit(IN_NAME_SIZE)),
                Text.pad_sequence(part3, IN_CONTEXT_SIZE).stream().limit(IN_CONTEXT_SIZE)).toList();
    }

    @Override
    public String predict(final List<Integer> predictSet) {
        if (this.tagClassifierFunc == null) {
            return this.tags.get(0);
        }
        final HashMap<String, org.tensorflow.Tensor> inputs = new HashMap<>() {
            {
                put("entity_input", ListIntegertoTFloat32(predictSet, 0, IN_ENTITY_SIZE));
                put("name_input", ListIntegertoTFloat32(predictSet, IN_ENTITY_SIZE, IN_ENTITY_SIZE + IN_NAME_SIZE));
                put("context_input", ListIntegertoTFloat32(predictSet, IN_ENTITY_SIZE + IN_NAME_SIZE,
                        IN_ENTITY_SIZE + IN_NAME_SIZE + IN_CONTEXT_SIZE));
            }
        };
        final org.tensorflow.Result result = this.tagClassifierFunc.call(inputs);
        return this.tags.get((int) TFloat32ToShujuTensor((TFloat32) result.get("tag_output").get()).argmax(0).item(0));
    }

    @Override
    public AbstractMap.SimpleImmutableEntry<List<Integer>, List<Integer>> buildTrainingSet(
            final String name, final List<String> entities, final List<String> context, final String label) {
        final List<Integer> key = Text.to_categorical(label, this.tags);
        final List<Integer> value = this.buildPredictSet(name, entities, context);
        return new AbstractMap.SimpleImmutableEntry<>(Text.pad_sequence(key, OUT_TAG_SIZE), value);
    }

    @Override
    public Process fit(final List<List<Integer>> trainingSet, final List<List<Integer>> validationSet)
            throws IOException {
        final Path kernelsPath = Path.of(System.getProperty("user.home"), "/.local/share/any2json/kernels");
        this.installKernels(kernelsPath);

        final Path kernelPath = kernelsPath.resolve("tf");
        if (!kernelPath.toFile().exists()) {
            throw new IOException("Kernel doesn'ty exist.");
        }

        final Path trainPath = Files.createTempDirectory("any2json").toAbsolutePath();
        final JSONArray list1 = JSON.newJSONArray();
        trainingSet.forEach(x -> {
            list1.append(JSON.parseJSONArray(x.toString()));
        });
        JSON.saveJSONArray(list1, trainPath.resolve("training.json").toString());

        final JSONArray list2 = JSON.newJSONArray();
        validationSet.forEach(x -> {
            list2.append(JSON.parseJSONArray(x.toString()));
        });
        JSON.saveJSONArray(list2, trainPath.resolve("validation.json").toString());

        final boolean isOsWindows = System.getProperty("os.name").contains("Windows");
        final String run_script = isOsWindows ? "run.bat" : "run.sh";

        final String dimensions = String.format("%d,%d,%d,%d", IN_ENTITY_SIZE, IN_NAME_SIZE, IN_CONTEXT_SIZE,
                OUT_TAG_SIZE);

        final ProcessBuilder processBuilder = new ProcessBuilder(kernelPath.resolve(run_script).toString(),
                "-V " + vocabulary.size(), "-s " + dimensions, "-t " + trainPath, "-m " + this.modelPath);
        processBuilder.directory(kernelPath.toFile());
        processBuilder.redirectErrorStream(true);

        return processBuilder.start();
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject result =super.toJSON();
        result.setJSONArray("vocabulary", JSON.<String>toJSONArray(this.vocabulary));
        result.setInt("ngram", this.ngrams);
        result.setJSONArray("lexicon", JSON.<String>toJSONArray(this.lexicon));
        result.setJSONArray("tags", JSON.<String>toJSONArray(this.tags));
        result.setJSONArray("requiredTags", JSON.<String>toJSONArray(this.requiredTags));
        result.setString("model", this.modelToJSONString(this.modelPath));
        return result;
    }

    private String modelToJSONString(final Path modelPath) {
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

    private void installKernels(final Path destPath) {
        try {
            if (destPath.toFile().exists()) {
                return;
            }
            final Path sourcePath = Path.of(LayexAndNetClassifier.class.getResource("/kernels").toURI());
            if (!sourcePath.toFile().isDirectory()) {
                return;
            }
            Arrays.asList(sourcePath.toFile().listFiles()).stream()
                    .forEach(k -> this.installOneKernel(k.toPath(), destPath.resolve(sourcePath.relativize(k.toPath()))));
        } catch (final URISyntaxException x) {
            throw new RuntimeException(x);
        }
    }

    private void installOneKernel(final Path srcPath, final Path destPath) {
        try {
            final boolean isOsLinux = System.getProperty("os.name").contains("Linux");

            final Path init_script;
            if (isOsLinux) {
                init_script = destPath.resolve("init.sh");
                init_script.toFile().setExecutable(true);
                destPath.resolve("run.sh").toFile().setExecutable(true);
            } else {
                init_script = destPath.resolve("init.bat");
            }

            Disk.copyDir(srcPath, destPath);

            final ProcessBuilder processBuilder = new ProcessBuilder(init_script.toString());
            processBuilder.directory(destPath.toFile());
            processBuilder.inheritIO();
            processBuilder.redirectErrorStream(true);
            processBuilder.start().waitFor();

        } catch (IOException | InterruptedException x) {
            throw new RuntimeException(x);
        }
    }

    private TFloat32 ListIntegertoTFloat32(final List<Integer> l, final int a, final int b) {
        final float[][] result = new float[1][b - a];
        for (int i = a, j = 0; i < b; i++, j++) {
            result[0][j] = (float) l.get(i);
        }
        return TFloat32.tensorOf(StdArrays.ndCopyOf(result));
    }

    private Tensor TFloat32ToShujuTensor(final TFloat32 t) {
        final float[] result = new float[(int) t.shape().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = t.getFloat(0, i);
        }
        return Tensor.create(result);
    }
}
