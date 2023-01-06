package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.SessionFunction;
import org.tensorflow.Signature;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class LayexAndNetClassifier implements ILayoutClassifier, ITagClassifier<List<Integer>>, AutoCloseable {

    private final List<String> vocabulary;
    private final int ngrams;
    private final List<String> lexicon;
    private final List<String> entities;
    private final Map<String, String> patterns;
    private final List<String> filters;
    private final List<String> tags;
    private final List<String> requiredTags;
    private final List<String> pivotEntityList;
    private final List<String> metaLayexes;
    private final List<String> dataLayexes;
    private final Path modelPath;

    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
    private String recipe;

    private float accuracy;
    private float mean;

    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;
    private final RegexComparer comparer;

    private final SavedModelBundle tagClassifierModel;
    private final SessionFunction tagClassifierFunc;
    private final boolean modelIsTemp;

    public LayexAndNetClassifier(final List<String> vocabulary, final int ngrams, final List<String> lexicon,
            final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> tags, final List<String> requiredTags, final List<String> pivotEntityList,
            final List<String> metaLayexes, final List<String> dataLayexes, final Path modelPath) {
        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.lexicon = lexicon;
        this.entities = entities;
        this.patterns = patterns;
        this.filters = filters;
        this.tags = tags;
        this.requiredTags = requiredTags;
        this.pivotEntityList = pivotEntityList;
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon) : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);
        this.comparer = new RegexComparer(this.patterns);

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
        this.vocabulary = JSON.<String>Stream(json.getJSONArray("vocabulary")).toList();
        this.ngrams = json.getInt("ngrams");
        this.lexicon = JSON.<String>Stream(json.getJSONArray("lexicon")).toList();
        this.entities = JSON.<String>Stream(json.getJSONArray("entities")).toList();
        this.patterns = JSON.<JSONObject>Stream(json.getJSONArray("patterns")).collect(Collectors.toMap(x -> x.getString("key"), x -> x.getString("value")));
        this.filters = JSON.<String>Stream(json.getJSONArray("filters")).toList();
        this.tags = JSON.<String>Stream(json.getJSONArray("tags")).toList();
        this.requiredTags = JSON.<String>Stream(json.getJSONArray("requiredTags")).toList();
        this.pivotEntityList = JSON.<String>Stream(json.getJSONArray("pivotEntityList")).toList();
        this.metaLayexes = JSON.<String>Stream(json.getJSONArray("metaLayexes")).toList();
        this.dataLayexes = JSON.<String>Stream(json.getJSONArray("dataLayexes")).toList();

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;

        this.tokenizer = (this.ngrams == 0) ? new ShingleTokenizer(this.lexicon) : new NgramTokenizer(this.ngrams);
        this.hasher = new VocabularyHasher(this.vocabulary);
        this.comparer = new RegexComparer(this.patterns);

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
    public void close() {
        if (this.modelIsTemp) {
            try {
                Disk.deleteDir(this.modelPath);
            } catch (IOException ignore) {
            }
        }
        if (this.tagClassifierModel != null) {
            this.tagClassifierModel.close();
        }
    }

    @Override
    public int getSampleCount() {
        return DocumentFactory.DEFAULT_SAMPLE_COUNT;
    }

    @Override
    public List<String> getEntityList() {
        return this.entities;
    }

    @Override
    public List<String> getPivotEntityList() {
        return this.pivotEntityList;
    }

    @Override
    public List<TableMatcher> getMetaMatcherList() {
        return this.metaMatchers;
    }

    @Override
    public void setMetaMatcherList(final List<TableMatcher> matchers) {
        this.metaMatchers = matchers;
    }

    @Override
    public List<TableMatcher> getDataMatcherList() {
        return this.dataMatchers;
    }

    @Override
    public void setDataMatcherList(final List<TableMatcher> matchers) {
        this.dataMatchers = matchers;
    }

    @Override
    public String getRecipe() {
        return this.recipe;
    }

    @Override
    public void setRecipe(final String recipe) {
        this.recipe = recipe;
    }

    @Override
    public String toEntityName(final String value) {
        return this.comparer.anonymize(value);
    }

    @Override
    public Optional<String> toEntityValue(final String value) {
        return this.comparer.find(value);
    }

    @Override
    public Tensor toEntityVector(final String value) {
        return Tensor.create(Text.to_categorical(value, this.entities, this.comparer).stream()
                .mapToDouble(x -> (double) x).toArray());
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
                Text.pad_sequence(part1, 10).stream(),
                Text.pad_sequence(part2, 5).stream()),
                Text.pad_sequence(part3, 100).stream()).toList();
    }

    @Override
    public String predict(final List<Integer> predictSet) {
        if (this.tagClassifierFunc == null) {
            return this.tags.get(0);
        }
        final HashMap<String, org.tensorflow.Tensor> inputs = new HashMap<>() {{
            put("entity_input", ListIntegertoTFloat32(predictSet, 0, 10));
            put("name_input", ListIntegertoTFloat32(predictSet, 10, 15));
            put("context_input", ListIntegertoTFloat32(predictSet, 15, 115));
        }};
        final Map<String, org.tensorflow.Tensor> result = this.tagClassifierFunc.call(inputs);
        return this.tags.get((int) TFloat32ToShujuTensor((TFloat32) result.get("tag_output")).argmax(0).item(0)); 
    }

    @Override
    public boolean fit(final List<List<Integer>> trainingSet, final List<List<Integer>> validationSet) {
        try {
            this.installKernels(Paths.get(System.getProperty("user.home") + "/.local/any2json/kernels"));

            final Path kernelPath = Paths.get(System.getProperty("user.home") + "/.local/any2json/kernels/tf");
            if (!kernelPath.toFile().exists()) {
                return false;
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

            final ProcessBuilder processBuilder = new ProcessBuilder(kernelPath.resolve("run.sh").toString(), "-s 10,5,100,32", "-t " + trainPath, "-m " + this.modelPath);
            processBuilder.directory(kernelPath.toFile());
            processBuilder.inheritIO();
            processBuilder.redirectErrorStream(true);

            final Process process = processBuilder.start();
            final int exitcode = process.waitFor();
            return exitcode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public float getAccuracy() {
        return this.accuracy;
    }

    @Override
    public float getMean() {
        return this.mean;
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject result = JSON.newJSONObject();
        result.setJSONArray("vocabulary", JSON.<String>toJSONArray(this.vocabulary));
        result.setInt("ngram", this.ngrams);
        result.setJSONArray("lexicon", JSON.<String>toJSONArray(this.lexicon));
        result.setJSONArray("entities", JSON.<String>toJSONArray(this.entities));
        result.setJSONArray("patterns", JSON.<String>toJSONArray(this.patterns));
        result.setJSONArray("filters", JSON.<String>toJSONArray(this.filters));
        result.setJSONArray("tags", JSON.<String>toJSONArray(this.tags));
        result.setJSONArray("requiredTags", JSON.<String>toJSONArray(this.requiredTags));
        result.setJSONArray("pivotEntityList", JSON.<String>toJSONArray(this.pivotEntityList));
        result.setJSONArray("metaLayexes", JSON.<String>toJSONArray(this.metaLayexes));
        result.setJSONArray("dataLayexes", JSON.<String>toJSONArray(this.dataLayexes));
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
            if(destPath.toFile().exists()) {
                return;
            }
            final Path sourcePath = Paths.get(LayexAndNetClassifier.class.getResource("/kernels").toURI());
            if (!sourcePath.toFile().isDirectory()) {
                return;
            }
            Arrays.asList(sourcePath.toFile().listFiles()).stream().forEach(k -> {
                try {
                    Disk.copyDir(k.toPath(), destPath.resolve(sourcePath.relativize(k.toPath())));
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            });
        } catch (final URISyntaxException x) {
            throw new RuntimeException(x);
        }
    }

    private TFloat32 ListIntegertoTFloat32(final List<Integer> l, final int a, final int b) {
        final float[][] result = new float[1][b - a];
        for(int i = a, j = 0; i < b; i++, j++) {
            result[0][j] = (float) l.get(i);
        }
        return TFloat32.tensorOf(StdArrays.ndCopyOf(result));
    }

    private Tensor TFloat32ToShujuTensor(final TFloat32 t) {
        final float[] result = new float[(int) t.shape().size(1)];
        for(int i = 0; i < result.length; i++) {
            result[i] = t.getFloat(0, i);
        }
        return Tensor.create(result);
    }
}
