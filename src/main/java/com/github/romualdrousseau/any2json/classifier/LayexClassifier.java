package com.github.romualdrousseau.any2json.classifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;

public class LayexClassifier implements ILayoutClassifier
{
    protected final List<String> vocabulary;
    protected final int ngrams;
    protected final List<String> lexicon;
    protected final List<String> entities;
    protected final Map<String, String> patterns;
    protected final List<String> filters;
    protected final List<String> pivotEntityList;
    protected final List<String> metaLayexes;
    protected final List<String> dataLayexes;

    protected final RegexComparer comparer;

    protected List<TableMatcher> metaMatchers;
    protected List<TableMatcher> dataMatchers;
    protected String recipe;

    public LayexClassifier(final List<String> vocabulary, final int ngrams, final List<String> lexicon,
            final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> pivotEntityList, final List<String> metaLayexes, final List<String> dataLayexes) {
        this.vocabulary = vocabulary;
        this.ngrams = ngrams;
        this.lexicon = lexicon;
        this.entities = entities;
        this.patterns = patterns;
        this.filters = filters;
        this.pivotEntityList = pivotEntityList;
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;

        this.comparer = new RegexComparer(this.patterns);
    }

    public LayexClassifier(final JSONObject json) {
        this.vocabulary = JSON.<String>Stream(json.getJSONArray("vocabulary")).toList();
        this.ngrams = json.getInt("ngrams");
        this.lexicon = JSON.<String>Stream(json.getJSONArray("lexicon")).toList();
        this.entities = JSON.<String>Stream(json.getJSONArray("entities")).toList();
        this.patterns = JSON.<JSONObject>Stream(json.getJSONArray("patterns"))
                .collect(Collectors.toMap(x -> x.getString("key"), x -> x.getString("value")));
        this.filters = JSON.<String>Stream(json.getJSONArray("filters")).toList();
        this.pivotEntityList = JSON.<String>Stream(json.getJSONArray("pivotEntityList")).toList();
        this.metaLayexes = JSON.<String>Stream(json.getJSONArray("metaLayexes")).toList();
        this.dataLayexes = JSON.<String>Stream(json.getJSONArray("dataLayexes")).toList();

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;

        this.comparer = new RegexComparer(this.patterns);
    }

    @Override
    public void close() throws Exception {
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
    public JSONObject toJSON() {
        final JSONObject result = JSON.newJSONObject();
        result.setJSONArray("vocabulary", JSON.<String>toJSONArray(this.vocabulary));
        result.setInt("ngram", this.ngrams);
        result.setJSONArray("lexicon", JSON.<String>toJSONArray(this.lexicon));
        result.setJSONArray("entities", JSON.<String>toJSONArray(this.entities));
        result.setJSONArray("patterns", JSON.<String>toJSONArray(this.patterns));
        result.setJSONArray("filters", JSON.<String>toJSONArray(this.filters));
        result.setJSONArray("pivotEntityList", JSON.<String>toJSONArray(this.pivotEntityList));
        result.setJSONArray("metaLayexes", JSON.<String>toJSONArray(this.metaLayexes));
        result.setJSONArray("dataLayexes", JSON.<String>toJSONArray(this.dataLayexes));
        return result;
    }
}
