package com.github.romualdrousseau.any2json.classifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.math.Tensor2D;
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

public class LayexAndNetClassifier implements ILayoutClassifier, ITagClassifier {
    public static final int BATCH_SIZE = 64;

    // public final static String[] MetaLayexesDefault = { "(v.$)+" };
    // public final static String[] DataLayexesDefault = {
    // "((e.*$)(vS.+$))(()(.{3,}$)())+(.{2}$)?",
    // "((v.*$)(vS.+$))((.{2}$)(.{3,}$)+())+(.{2}$)?",
    // "(()(ES.+$))((sS.+$)(S.{2,}$)+())+(.{2}$)?",
    // "(()(ES.+$))(()(.{3,}$)())+(.{2}$)?"
    // };

    private final NgramList ngrams;
    private final RegexList entities;
    private final StringList tags;
    private final StopWordList stopwords;
    private final List<String> requiredTags;
    private final List<String> pivotEntityList;
    private final List<Layex> metaLayexes;
    private final List<Layex> dataLayexes;

    private final List<TableMatcher> metaMatchers;
    private final List<TableMatcher> dataMatchers;

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

    public int getSampleCount() {
        return DocumentFactory.DEFAULT_SAMPLE_COUNT;
    }

    public StopWordList getStopWordList() {
        return this.stopwords;
    }

    @Override
    public RegexList getEntityList() {
        return this.entities;
    }

    @Override
    public NgramList getWordList() {
        return this.ngrams;
    }

    @Override
    public StringList getTagList() {
        return this.tags;
    }

    @Override
    public List<String> getRequiredTagList() {
        return this.requiredTags;
    }

    @Override
    public List<TableMatcher> getMetaMatcherList() {
        return this.metaMatchers;
    }

    @Override
    public List<TableMatcher> getDataMatcherList() {
        return this.dataMatchers;
    }

    @Override
    public List<String> getPivotEntityList() {
        return this.pivotEntityList;
    }

    @Override
    public String getRecipe() {
        return this.recipe;
    }

    @Override
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    @Override
    public float getMean() {
        return this.mean;
    }

    @Override
    public float getAccuracy() {
        return this.accuracy;
    }

    @Override
    public DataRow buildPredictRow(final String name, final Iterable<String> entities, final Iterable<String> context) {
        final Tensor1D entityVector = new Tensor1D(this.getEntityList().getVectorSize());
        entities.forEach(entity -> {
            final int i = this.getEntityList().ordinal(entity);
            if (i != -1) {
                entityVector.set(i, 1);
            }
        });

        final Tensor1D wordVector = this.getWordList().word2vec(name);

        final Tensor1D contextVector = wordVector.copy().zero();
        context.forEach(other -> contextVector.add(this.getWordList().word2vec(other)));
        final Tensor1D word_mask = wordVector.copy().ones().sub(wordVector);
        contextVector.mul(word_mask).constrain(0, 1);

        return new DataRow().addFeature(entityVector).addFeature(wordVector).addFeature(contextVector);
    }

    @Override
    public DataRow buildTrainingRow(final String name, final Iterable<String> entities, final Iterable<String> context,
            final String tag, final boolean ensureWordsExists) {
        if (ensureWordsExists) {
            context.forEach(this.getWordList()::add);
        }
        final Tensor1D label = this.getTagList().word2vec(tag);
        return this.buildPredictRow(name, entities, context).setLabel(label);
    }

    @Override
    public void fit(final DataSet trainingSet, final DataSet validationSet) {
        final float n = trainingSet.rows().size();
        if (n == 0.0f) {
            return;
        }

        this.accuracy = 0.0f;
        this.mean = 0.0f;

        // Train

        for (int i = 0; i < trainingSet.rows().size();) {

            this.optimizer.zeroGradients();

            final int batchSize = Math.min(trainingSet.rows().size() - i, BATCH_SIZE);
            for (int j = 0; j < batchSize; j++) {
                final DataRow row = trainingSet.rows().get(i++);

                final Tensor2D input = new Tensor2D(row.featuresAsOneVector(), true);
                final Tensor2D target = new Tensor2D(row.label(), true);

                final Layer output = this.model.model(input, true);
                final Loss loss = this.loss.loss(output, target);

                this.optimizer.minimize(loss);
            }

            this.optimizer.step();
        }

        // Validate

        for (final DataRow row : validationSet.rows()) {
            final Tensor2D input = new Tensor2D(row.featuresAsOneVector(), true);
            final Tensor2D target = new Tensor2D(row.label(), true);

            final Layer output = this.model.model(input);
            final Loss loss = this.loss.loss(output, target);

            final boolean isCorrect = output.detach().argmax(0, 0) == target.argmax(0, 0);
            this.accuracy += isCorrect ? 1 : 0;
            this.mean += loss.getValue().flatten(0, 0);
        }

        final float total = validationSet.rows().size();
        this.accuracy /= total;
        this.mean /= total;
    }

    @Override
    public String predict(final DataRow row) {
        final Tensor2D input = new Tensor2D(row.featuresAsOneVector(), true);
        final Tensor2D output = this.model.model(input).detach();

        int tagIndex = output.argmax(0, 0);
        if (tagIndex >= this.tags.size()) {
            tagIndex = 0;
        }

        return this.tags.get(tagIndex);
    }

    public List<Layex> getMetaLayexList() {
        return this.metaLayexes;
    }
    
    public List<Layex> getDataLayexList() {
        return this.dataLayexes;
    }

    public Model getModel() {
        return this.model;
    }

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

    public String dumpDataSet(final DataSet dataset) {
        final StringBuilder result = new StringBuilder();
        result.append("============================ DUMP TRAININSET ============================\n");
        for (final DataRow row : dataset.rows()) {
            result.append(this.dumpDataRow(dataset, row));
        }
        result.append("================================== END ==================================\n");
        return result.toString();
    }

    public String dumpDataRow(final DataSet dataset, final DataRow row) {
        final StringBuilder result = new StringBuilder();

        Tensor1D v = row.features().get(0);
        boolean firstPass = true;
        for (int i = 0; i < v.shape[0]; i++) {
            if (v.get(i) == 1.0f) {
                final String e = this.getEntityList().get(i);
                if (e != null) {
                    if (firstPass) {
                        firstPass = false;
                    } else {
                        result.append(":");
                    }
                    result.append(e);
                }
            }
        }

        result.append(",");

        v = row.features().get(1);
        firstPass = true;
        for (int i = 0; i < v.shape[0]; i++) {
            if (v.get(i) == 1.0f) {
                final String w = this.getWordList().get(i);
                if (w != null) {
                    if (firstPass) {
                        firstPass = false;
                    } else {
                        result.append(":");
                    }
                    result.append(w);
                }
            }
        }

        result.append(",");

        v = row.features().get(2);
        firstPass = true;
        for (int i = 0; i < v.shape[0]; i++) {
            if (v.get(i) == 1.0f) {
                final String w = this.getWordList().get(i);
                if (w != null) {
                    if (firstPass) {
                        firstPass = false;
                    } else {
                        result.append(":");
                    }
                    result.append(w);
                }
            }
        }

        result.append(",");

        final Tensor1D l = row.label();

        firstPass = true;
        for (int i = 0; i < 16; i++) {
            if (l.get(i) == 1.0f) {
                if (firstPass) {
                    result.append(this.getTagList().get(i));
                    firstPass = false;
                } else {
                    result.append(":").append(this.getTagList().get(i));
                }
            }
        }

        result.append("\n");

        return result.toString();
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
