package com.github.romualdrousseau.any2json.classifiers;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.layex.Layex;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONArray;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Scalar;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.ml.nn.Layer;
import com.github.romualdrousseau.shuju.ml.nn.LayerBuilder;
import com.github.romualdrousseau.shuju.ml.nn.Loss;
import com.github.romualdrousseau.shuju.ml.nn.Model;
import com.github.romualdrousseau.shuju.ml.nn.Optimizer;
import com.github.romualdrousseau.shuju.ml.nn.activation.LeakyRelu;
import com.github.romualdrousseau.shuju.ml.nn.activation.Softmax;
import com.github.romualdrousseau.shuju.ml.nn.loss.SoftmaxCrossEntropy;
import com.github.romualdrousseau.shuju.ml.nn.normalizer.BatchNormalizer;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.OptimizerAdamBuilder;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public class NGramNNClassifier implements ITagClassifier {
    private NgramList ngrams;
    private RegexList entities;
    private StopWordList stopwords;
    private StringList tags;
    private String[] requiredTags;
    private Model model;
    private Optimizer optimizer;
    private Loss criterion;
    private float accuracy;
    private float mean;
    private List<LayexMatcher> metaLayexes;
    private List<LayexMatcher> dataLayexes;

    private final static String[] metaLayexesDefault = { "(v[v|e|s]$)+" };

    private final static String[] dataLayexesDefault = {
            "((e[v|e|s]*$)(v[v|e][v|e|s]+$))(()([v|e|s]{2}[v|e|s]+$))+([v|e|s]{2}$)?",
            "((v[v|e|s]*$)(v[v|e][v|e|s]+$))(([v|e|s]{2}$)([v|e|s]{2}[v|e|s]+$)+)+([v|e|s]{2}$)?",
            "(()([v|s][v|e][v|e|s]+$))((s[v|e][v|e|s]+$)([v|e][v|e|s][v|e|s]+$)+)+([v|e|s]{2}$)?",
            "(()([v|s][v|e][v|e|s]+$))(()([v|e|s]{2}[v|e|s]+$))+([v|e|s]{2}$)?" };

    public NGramNNClassifier(NgramList ngrams, RegexList entities, StopWordList stopwords, StringList tags) {
        this(ngrams, entities, stopwords, tags, null);
    }

    public NGramNNClassifier(NgramList ngrams, RegexList entities, StopWordList stopwords, StringList tags,
            String[] requiredTags) {
        this.accuracy = 0.0f;
        this.mean = 1.0f;
        this.ngrams = ngrams;
        this.entities = entities;
        this.stopwords = stopwords;
        this.tags = tags;
        this.requiredTags = requiredTags;
        this.buildModel();

        this.metaLayexes = new ArrayList<LayexMatcher>();
        for (String layex : metaLayexesDefault) {
            this.metaLayexes.add(new Layex(layex).compile());
        }

        this.dataLayexes = new ArrayList<LayexMatcher>();
        for (String layex : dataLayexesDefault) {
            this.dataLayexes.add(new Layex(layex).compile());
        }
    }

    public NGramNNClassifier(JSONObject json) {
        this(json, null, null);
    }

    public NGramNNClassifier(JSONObject json, String[] metaLayexes, String[] dataLayexes) {
        this(new NgramList(json.getJSONObject("ngrams")), new RegexList(json.getJSONObject("entities")),
                new StopWordList(json.getJSONArray("stopwords")), new StringList(json.getJSONObject("tags")), null);

        JSONArray requiredTags = json.getJSONObject("tags").getJSONArray("requiredTypes");
        if (requiredTags.size() > 0) {
            this.requiredTags = new String[requiredTags.size()];
            for (int i = 0; i < requiredTags.size(); i++) {
                this.requiredTags[i] = requiredTags.getString(i);
            }
        }

        this.model.fromJSON(json.getJSONArray("model"));

        if(metaLayexes != null) {
            this.metaLayexes = new ArrayList<LayexMatcher>();
            for (String layex : metaLayexes) {
                this.metaLayexes.add(new Layex(layex).compile());
            }
        }

        if(dataLayexes != null) {
            this.dataLayexes = new ArrayList<LayexMatcher>();
            for (String layex : dataLayexes) {
                this.dataLayexes.add(new Layex(layex).compile());
            }
        }
    }

    public int getSampleCount() {
        return DocumentFactory.DEFAULT_SAMPLE_COUNT;
    }

    public StopWordList getStopWordList() {
        return this.stopwords;
    }

    public RegexList getEntityList() {
        return this.entities;
    }

    public NgramList getWordList() {
        return this.ngrams;
    }

    public StringList getTagList() {
        return this.tags;
    }

    public String[] getRequiredTagList() {
        return this.requiredTags;
    }

    public List<LayexMatcher> getMetaLayexes() {
        return this.metaLayexes;
    }

    public List<LayexMatcher> getDataLayexes() {
        return this.dataLayexes;
    }

    public Model getModel() {
        return this.model;
    }

    public float getMean() {
        return this.mean;
    }

    public float getAccuracy() {
        return this.accuracy;
    }

    public void fit(DataSet dataset) {
        if (dataset.rows().size() == 0) {
            return;
        }

        final float total = dataset.shuffle().rows().size();

        this.accuracy = 0.0f;
        this.mean = 0.0f;

        this.optimizer.zeroGradients();

        for (DataRow data : dataset.rows()) {
            Vector input = data.featuresAsOneVector();
            Vector target = data.label();

            Layer output = this.model.model(input);
            Loss loss = this.criterion.loss(output, target);

            if (output.detach().argmax(0) != target.argmax()) {
                loss.backward();
            } else {
                this.accuracy++;
            }

            this.mean += loss.getValue().flatten(0);
            if (Float.isNaN(this.mean)) {
                this.mean = total;
            }
        }

        this.optimizer.step();

        this.accuracy = this.accuracy / total;
        this.mean = Scalar.constrain(this.mean / total, 0, 1);
    }

    // public void fit(DataSet dataset) {
    // if (dataset.rows().size() == 0) {
    // return;
    // }

    // final int total = dataset.shuffle().rows().size();
    // final int nCount = Math.min(10, total);
    // final int slice = total / nCount;

    // this.accuracy = 0.0f;
    // this.mean = 0.0f;

    // for (int n = 0; n < nCount; n++) {
    // int d1 = total - slice * (n + 1);
    // int d2 = total - slice * n;
    // DataSet trainingSet = dataset.subset(0, d1).join(dataset.subset(d2, total));
    // DataSet testSet = dataset.subset(d1, d2);

    // this.optimizer.zeroGradients();

    // for (DataRow data : trainingSet.rows()) {
    // Vector input = data.featuresAsOneVector();
    // Vector target = data.label();

    // Layer output = this.model.model(input);
    // Loss loss = this.criterion.loss(output, target);

    // if (output.detach().argmax(0) != target.argmax()) {
    // loss.backward();
    // }
    // }

    // this.optimizer.step();

    // for (DataRow data : testSet.rows()) {
    // Vector input = data.featuresAsOneVector();
    // Vector target = data.label();

    // Layer output = this.model.model(input);
    // Loss loss = this.criterion.loss(output, target);

    // if (output.detach().argmax(0) == target.argmax()) {
    // this.accuracy++;
    // }

    // this.mean += loss.getValue().flatten(0);
    // if (Float.isNaN(this.mean)) {
    // this.mean = (float) (slice * nCount);
    // }
    // }
    // }

    // this.accuracy = Scalar.constrain(this.accuracy / (float) (slice * nCount), 0,
    // 1);
    // this.mean = Scalar.constrain(this.mean / (float) (slice * nCount), 0, 1);
    // }

    public String predict(DataRow row) {
        Vector input = row.featuresAsOneVector();
        Vector output = this.model.model(input).detachAsVector();

        int tagIndex = output.argmax();
        if (tagIndex >= this.tags.size()) {
            tagIndex = 0;
        }
        return this.tags.get(tagIndex);
    }

    public JSONObject toJSON() {
        JSONArray requiredTags = JSON.newJSONArray();
        if (this.requiredTags != null) {
            for (int i = 0; i < this.requiredTags.length; i++) {
                requiredTags.append(this.requiredTags[i]);
            }
        }

        JSONObject tags = this.tags.toJSON();
        tags.setJSONArray("requiredTypes", requiredTags);

        JSONObject json = JSON.newJSONObject();
        json.setJSONObject("ngrams", this.ngrams.toJSON());
        json.setJSONObject("entities", this.entities.toJSON());
        json.setJSONArray("stopwords", this.stopwords.toJSON());
        json.setJSONObject("tags", tags);
        json.setJSONArray("model", this.model.toJSON());
        return json;
    }

    public String dumpDataSet(DataSet dataset) {
        StringBuilder result = new StringBuilder();

        for (DataRow row : dataset.rows()) {
            Vector v = row.featuresAsOneVector();

            boolean firstPass = true;
            for (int i = 0; i < 24; i++) {
                if (v.get(i) == 1.0f) {
                    if (firstPass) {
                        result.append(this.getEntityList().get(i));
                        firstPass = false;
                    } else {
                        result.append(":").append(this.getEntityList().get(i));
                    }
                }
            }

            result.append(",");
            firstPass = true;
            for (int i = 24; i < 524; i++) {
                if (v.get(i) == 1.0f) {
                    if (firstPass) {
                        result.append(this.getWordList().get(i - 24));
                        firstPass = false;
                    } else {
                        result.append(":").append(this.getWordList().get(i - 24));
                    }
                }
            }

            result.append(",");
            firstPass = true;
            for (int i = 524; i < 1024; i++) {
                if (v.get(i) == 1.0f) {
                    if (firstPass) {
                        result.append(this.getWordList().get(i - 524));
                        firstPass = false;
                    } else {
                        result.append(":").append(this.getWordList().get(i - 524));
                    }
                }
            }

            Vector l = row.label();
            result.append(",");
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
        }

        return result.toString();
    }

    private void buildModel() {
        final int inputCount = this.entities.getVectorSize() + 2 * this.ngrams.getVectorSize();
        final int hiddenCount = inputCount / 2;
        final int outputCount = this.tags.getVectorSize();

        final Layer layer1 = new LayerBuilder().setInputUnits(inputCount).setUnits(hiddenCount)
                .setActivation(new LeakyRelu()).setNormalizer(new BatchNormalizer()).build();

        final Layer layer2 = new LayerBuilder().setInputUnits(hiddenCount).setUnits(outputCount)
                .setActivation(new Softmax()).build();

        this.model = new Model().add(layer1).add(layer2);

        this.optimizer = new OptimizerAdamBuilder().build(this.model);

        this.criterion = new Loss(new SoftmaxCrossEntropy());
    }
}
