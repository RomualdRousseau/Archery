package com.github.romualdrousseau.any2json.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.math.deprecated.Tensor1D;
import com.github.romualdrousseau.shuju.math.deprecated.Tensor2D;
import com.github.romualdrousseau.shuju.ml.nn.Layer;
import com.github.romualdrousseau.shuju.ml.nn.Loss;
import com.github.romualdrousseau.shuju.ml.nn.Model;
import com.github.romualdrousseau.shuju.ml.nn.Optimizer;
import com.github.romualdrousseau.shuju.ml.nn.activation.LeakyRelu;
import com.github.romualdrousseau.shuju.ml.nn.activation.Softmax;
import com.github.romualdrousseau.shuju.ml.nn.layer.builder.ActivationBuilder;
import com.github.romualdrousseau.shuju.ml.nn.layer.builder.BatchNormalizerBuilder;
import com.github.romualdrousseau.shuju.ml.nn.layer.builder.DenseBuilder;
import com.github.romualdrousseau.shuju.ml.nn.loss.SoftmaxCrossEntropy;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.OptimizerAdamBuilder;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public class LayexAndNetClassifier implements ILayoutClassifier, ITagClassifier<DataRow> {
    public static final int BATCH_SIZE = 64;

    private final NgramList ngrams;
    private final RegexList entities;
    private final StringList tags;
    private final StopWordList stopwords;
    private final List<String> requiredTags;
    private final List<String> pivotEntityList;
    private final List<Layex> metaLayexes;
    private final List<Layex> dataLayexes;

    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
    private String recipe;

    private Model model;
    private Optimizer optimizer;
    private Loss loss;
    private float accuracy;
    private float mean;

    public LayexAndNetClassifier(final NgramList ngrams, final RegexList entities, final StopWordList stopwords,
            final StringList tags, final List<String> requiredTags, final List<String> pivotEntityList,
            final List<String> metaLayexes, final List<String> dataLayexes, final String recipe) {
        this.ngrams = ngrams;
        this.entities = entities;
        this.stopwords = stopwords;
        this.tags = tags;
        this.requiredTags = requiredTags;
        this.pivotEntityList = pivotEntityList;
        this.metaLayexes = metaLayexes.stream().map(Layex::new).collect(Collectors.toCollection(ArrayList::new));
        this.dataLayexes = dataLayexes.stream().map(Layex::new).collect(Collectors.toCollection(ArrayList::new));

        this.metaMatchers = this.metaLayexes.stream().map(Layex::compile).collect(Collectors.toCollection(ArrayList::new));
        this.dataMatchers = this.dataLayexes.stream().map(Layex::compile).collect(Collectors.toCollection(ArrayList::new));
        this.recipe = recipe;

        this.buildModel();
    }

    public LayexAndNetClassifier(final JSONObject json) {
        this(new NgramList(json.getJSONObject("ngrams")),
                new RegexList(json.getJSONObject("entities")),
                new StopWordList(json.getJSONArray("stopwords")),
                new StringList(json.getJSONObject("tags")),
                unmarshallStringList(json.getJSONObject("tags").getJSONArray("requiredTags")),
                unmarshallStringList(json.getJSONObject("entities").getJSONArray("pivotEntities")),
                unmarshallStringList(json.getJSONArray("layexes"), "META"),
                unmarshallStringList(json.getJSONArray("layexes"), "DATA"),
                null);
        this.model.fromJSON(json.getJSONArray("model"));
    }

    @Override
    public int getSampleCount() {
        return DocumentFactory.DEFAULT_SAMPLE_COUNT;
    }

    @Override
    public List<String> getEntityList() {
        return this.entities.values();
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
        return this.entities.anonymize(value);
    }

    @Override
    public Optional<String> toEntityValue(String value) {
        return Optional.ofNullable(this.entities.find(value));
    }

    @Override
    public Tensor toEntityVector(String value) {
        return Tensor.create(this.entities.word2vec(value).data);
    }

    @Override
    public List<String> getTagList() {
        return this.tags.values();
    }

    @Override
    public List<String> getRequiredTagList() {
        return this.requiredTags;
    }

    @Override
    public DataRow buildPredictSet(final String name, final List<String> entities, final List<String> context) {
        final Tensor1D entityVector = new Tensor1D(this.entities.getVectorSize());
        entities.forEach(entity -> {
            final int i = this.entities.ordinal(entity);
            if (i != -1) {
                entityVector.set(i, 1);
            }
        });

        final Tensor1D wordVector = this.ngrams.word2vec(name);

        final Tensor1D contextVector = wordVector.copy().zero();
        context.forEach(other -> contextVector.add(this.ngrams.word2vec(other)));
        final Tensor1D word_mask = wordVector.copy().ones().sub(wordVector);
        contextVector.mul(word_mask).constrain(0, 1);

        return new DataRow().addFeature(entityVector).addFeature(wordVector).addFeature(contextVector);
    }

    @Override
    public String predict(final DataRow predictSet) {
        final Tensor2D input = new Tensor2D(predictSet.featuresAsOneVector(), true);
        final Tensor2D output = this.model.model(input).detach();

        int tagIndex = output.argmax(0, 0);
        if (tagIndex >= this.tags.size()) {
            tagIndex = 0;
        }

        return this.tags.get(tagIndex);
    }

    @Override
    public void fit(final List<DataRow> trainingSet, final List<DataRow> validationSet) {
        final float n = trainingSet.size();
        if (n == 0.0f) {
            return;
        }

        this.accuracy = 0.0f;
        this.mean = 0.0f;

        // Train

        for (int i = 0; i < trainingSet.size();) {

            this.optimizer.zeroGradients();

            final int batchSize = Math.min(trainingSet.size() - i, BATCH_SIZE);
            for (int j = 0; j < batchSize; j++) {
                final DataRow row = trainingSet.get(i++);

                final Tensor2D input = new Tensor2D(row.featuresAsOneVector(), true);
                final Tensor2D target = new Tensor2D(row.label(), true);

                final Layer output = this.model.model(input, true);
                final Loss loss = this.loss.loss(output, target);

                this.optimizer.minimize(loss);
            }

            this.optimizer.step();
        }

        // Validate

        for (final DataRow row : validationSet) {
            final Tensor2D input = new Tensor2D(row.featuresAsOneVector(), true);
            final Tensor2D target = new Tensor2D(row.label(), true);

            final Layer output = this.model.model(input);
            final Loss loss = this.loss.loss(output, target);

            final boolean isCorrect = output.detach().argmax(0, 0) == target.argmax(0, 0);
            this.accuracy += isCorrect ? 1 : 0;
            this.mean += loss.getValue().flatten(0, 0);
        }

        final float total = validationSet.size();
        this.accuracy /= total;
        this.mean /= total;
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
        final JSONArray jsonRequiredTags = JSON.newJSONArray();
        this.requiredTags.forEach(x -> jsonRequiredTags.append(x));
        final JSONObject jsonTags = this.tags.toJSON();
        jsonTags.setJSONArray("requiredTags", jsonRequiredTags);

        final JSONArray jsonPivotEntities = JSON.newJSONArray();
        this.pivotEntityList.forEach(x -> jsonPivotEntities.append(x));
        final JSONObject jsonEntities = this.entities.toJSON();
        jsonEntities.setJSONArray("pivotEntities", jsonPivotEntities);

        final JSONArray jsonLayexes = JSON.newJSONArray();
        this.metaLayexes.forEach(x -> {
            final JSONObject jsonMeta = JSON.newJSONObject();
            jsonMeta.setString("type", "META");
            jsonMeta.setString("layex", x.toString());
            jsonLayexes.append(jsonMeta);
        });
        this.dataLayexes.forEach(x -> {
            final JSONObject jsonData = JSON.newJSONObject();
            jsonData.setString("type", "DATA");
            jsonData.setString("layex", x.toString());
            jsonLayexes.append(jsonData);
        });

        final JSONObject json = JSON.newJSONObject();
        json.setJSONObject("ngrams", this.ngrams.toJSON());
        json.setJSONObject("entities", jsonEntities);
        json.setJSONArray("stopwords", this.stopwords.toJSON());
        json.setJSONObject("tags", jsonTags);
        json.setJSONArray("layexes", jsonLayexes);
        json.setJSONArray("model", this.model.toJSON());
        return json;
    }

    private void buildModel() {
        final int inputCount = this.entities.getVectorSize() + 2 * this.ngrams.getVectorSize();
        final int hiddenCount1 = inputCount * 3 / 4;
        final int hiddenCount2 = inputCount / 4;
        final int outputCount = this.tags.getVectorSize();

        this.model = new Model()
                .add(new DenseBuilder().setInputUnits(inputCount).setUnits(hiddenCount1))
                .add(new ActivationBuilder().setActivation(new LeakyRelu()))
                .add(new DenseBuilder().setUnits(hiddenCount2))
                .add(new ActivationBuilder().setActivation(new LeakyRelu()))
                .add(new BatchNormalizerBuilder())
                .add(new DenseBuilder().setUnits(outputCount))
                .add(new ActivationBuilder().setActivation(new Softmax()));

        this.optimizer = new OptimizerAdamBuilder().build(this.model);

        this.loss = new Loss(new SoftmaxCrossEntropy());
        this.accuracy = 0.0f;
        this.mean = 1.0f;
    }

    private static List<String> unmarshallStringList(final JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return Collections.emptyList();
        }
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    private static List<String> unmarshallStringList(final JSONArray jsonArray, final String query) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return Collections.emptyList();
        }
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("type").equals(query)) {
                list.add(jsonObject.getString("layex"));
            }
        }
        return list;
    }
}
