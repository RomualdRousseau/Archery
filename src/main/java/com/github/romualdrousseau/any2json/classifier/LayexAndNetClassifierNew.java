package com.github.romualdrousseau.any2json.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;

import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.preprocessing.hasher.VocabularyHasher;
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

    public LayexAndNetClassifierNew(final List<String> vocabulary, final List<String> lexicon, final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> tags, final List<String> requiredTags, final List<String> pivotEntityList,
            final List<String> metaLayexes, final List<String> dataLayexes) {
        this.entities = entities;
        this.filters = filters;
        this.tags = tags;
        this.requiredTags = requiredTags;
        this.pivotEntityList = pivotEntityList;

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).collect(Collectors.toCollection(ArrayList::new));
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).collect(Collectors.toCollection(ArrayList::new));
        this.recipe = null;

        this.tokenizer = new ShingleTokenizer(lexicon);
        this.hasher = new VocabularyHasher(vocabulary);
        this.comparer = new RegexComparer(patterns);
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
    public void setMetaMatcherList(List<TableMatcher> matchers) {
        this.metaMatchers = matchers;
    }

    @Override
    public List<TableMatcher> getDataMatcherList() {
        return this.dataMatchers;
    }

    @Override
    public void setDataMatcherList(List<TableMatcher> matchers) {
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
    public String toEntityName(String value) {
        return this.comparer.anonymize(value);
    }

    @Override
    public Optional<String> toEntityValue(String value) {
        return this.comparer.find(value);
    }

    @Override
    public Tensor toEntityVector(String value) {
        return Tensor.create(Text.to_categorical(Arrays.asList(value), this.entities, this.comparer).stream().mapToDouble(x -> (double) x).toArray());
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
    public List<Integer> buildPredictRow(String name, List<String> entities, List<String> context) {
        final List<Integer> part1 = Text.to_categorical(entities, this.entities);
        final List<Integer> part2 = Text.one_hot(name, this.filters, this.tokenizer, this.hasher);
        final List<Integer> part3 = context.stream()
                .filter(x -> !x.equals(name))
                .flatMap(x -> Text.one_hot(x, this.filters, this.tokenizer, this.hasher).stream())
                .distinct().sorted().toList();
        return Stream.concat(Stream.concat(
            Text.pad_sequence(part1, 10).stream(),
            Text.pad_sequence(part2, 5).stream()),
            Text.pad_sequence(part3, 20).stream()).toList();
    }

    @Override
    public String predict(final String name, final List<String> entities, final List<String> context) {
        this.buildPredictRow(name, entities, context);
        return "none";
    }

    @Override
    public void fit(List<List<Integer>> trainingSet, List<List<Integer>> validationSet) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public float getAccuracy() {
        return this.accuracy;
    }

    @Override
    public float getMean() {
        return this.mean;
    }
}
