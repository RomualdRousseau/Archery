package com.github.romualdrousseau.any2json.classifier;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.NgramTokenizer;
import com.github.romualdrousseau.shuju.preprocessing.tokenizer.ShingleTokenizer;

public class LayexAndNetClassifierNew implements ILayoutClassifier, ITagClassifier<List<Integer>> {

    private final List<String> entities;
    private final List<String> filters;
    private final List<String> tags;
    private final List<String> requiredTags;
    private final List<String> pivotEntityList;

    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
    private String recipe;

    private float accuracy;
    private float mean;

    private final Text.ITokenizer tokenizer;
    private final Text.IHasher hasher;
    private final RegexComparer comparer;
    private final SessionFunction tagClassifierFunc;

    public LayexAndNetClassifierNew(final List<String> vocabulary, final int ngrams, final List<String> lexicon,
            final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> tags, final List<String> requiredTags, final List<String> pivotEntityList,
            final List<String> metaLayexes, final List<String> dataLayexes, final JSONObject parameters) {
        this.entities = entities;
        this.filters = filters;
        this.tags = tags;
        this.requiredTags = requiredTags;
        this.pivotEntityList = pivotEntityList;

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;

        this.tokenizer = (ngrams == 0) ? new ShingleTokenizer(lexicon) : new NgramTokenizer(ngrams);
        this.hasher = new VocabularyHasher(vocabulary);
        this.comparer = new RegexComparer(patterns);

        if (Path.of(parameters.getString("model")).toFile().exists()) {
            SavedModelBundle model = SavedModelBundle.load(parameters.getString("model"), "serve");
            this.tagClassifierFunc = model.function(Signature.DEFAULT_KEY);
        } else {
            this.tagClassifierFunc = null;
        }
    }

    public LayexAndNetClassifierNew(final JSONObject json) {
        this.entities = Collections.emptyList();
        this.filters = Collections.emptyList();
        this.tags = Collections.emptyList();
        this.requiredTags = Collections.emptyList();
        this.pivotEntityList = Collections.emptyList();

        this.metaMatchers = Collections.emptyList();
        this.dataMatchers = Collections.emptyList();
        this.recipe = null;

        this.tokenizer = new ShingleTokenizer(Collections.emptyList());
        this.hasher = new VocabularyHasher(Collections.emptyList());
        this.comparer = new RegexComparer(Collections.emptyMap());

        this.tagClassifierFunc = null;
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
        Map<String, org.tensorflow.Tensor> result = this.tagClassifierFunc.call(new HashMap<String, org.tensorflow.Tensor>() {{
            put("input_1", ListIntegertoTFloat32(predictSet, 0, 10));
            put("embedding_input", ListIntegertoTFloat32(predictSet, 10, 15));
            put("embedding_1_input", ListIntegertoTFloat32(predictSet, 15, 115));
        }});
        return this.tags.get((int) TFloat32ToTensor((TFloat32) result.get("dense_2")).argmax(0).item(0)); 
    }

    @Override
    public void fit(final List<List<Integer>> trainingSet, final List<List<Integer>> validationSet) {
        JSONArray list1 = JSON.newJSONArray();
        trainingSet.forEach(x -> { 
            list1.append(JSON.parseJSONArray(x.toString()));
        });
        JSON.saveJSONArray(list1, "training.json");

        JSONArray list2 = JSON.newJSONArray();
        validationSet.forEach(x -> { 
            list2.append(JSON.parseJSONArray(x.toString()));
        });
        JSON.saveJSONArray(list2, "validation.json");

        this.accuracy = 1.0f;
    }

    private TFloat32 ListIntegertoTFloat32(List<Integer> l, int a, int b) {
        float[][] result = new float[1][b - a];
        for(int i = a, j = 0; i < b; i++, j++) {
            result[0][j] = (float) l.get(i);
        }
        return TFloat32.tensorOf(StdArrays.ndCopyOf(result));
    }

    private Tensor TFloat32ToTensor(TFloat32 t) {
        float[] result = new float[(int) t.shape().size(1)];
        for(int i = 0; i < result.length; i++) {
            result[i] = t.getFloat(0, i);
        }
        return Tensor.create(result);
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
        JSONObject result = JSON.newJSONObject();
        result.set("model", "/home/romuald/DataLoaderStudio/sales-spanish/target/model");
        return result;
    }
}
