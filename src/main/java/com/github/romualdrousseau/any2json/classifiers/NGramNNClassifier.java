package com.github.romualdrousseau.any2json.classifiers;

import com.github.romualdrousseau.shuju.math.Scalar;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.ml.nn.Model;
import com.github.romualdrousseau.shuju.ml.nn.Layer;
import com.github.romualdrousseau.shuju.ml.nn.Optimizer;
import com.github.romualdrousseau.shuju.ml.nn.Loss;
import com.github.romualdrousseau.shuju.ml.nn.LayerBuilder;
import com.github.romualdrousseau.shuju.ml.nn.activation.LeakyRelu;
import com.github.romualdrousseau.shuju.ml.nn.activation.Softmax;
import com.github.romualdrousseau.shuju.ml.nn.normalizer.BatchNormalizer;
import com.github.romualdrousseau.shuju.ml.nn.loss.SoftmaxCrossEntropy;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.OptimizerAdamBuilder;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.shuju.DataRow;

import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public class NGramNNClassifier implements ITagClassifier {
    private NgramList ngrams;
    private RegexList entities;
    private StopWordList stopwords;
    private StringList tags;
    private Model model;
    private Optimizer optimizer;
    private Loss criterion;
    private float accuracy;
    private float mean;

    public NGramNNClassifier(NgramList ngrams, RegexList entities, StopWordList stopwords, StringList tags) {
        this.accuracy = 0.0f;
        this.mean = 1.0f;
        this.ngrams = ngrams;
        this.entities = entities;
        this.stopwords = stopwords;
        this.tags = tags;
        this.buildModel();
    }

    public int getSampleCount() {
        return 30;
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

    public Model getModel() {
        return this.model;
    }

    public float getMean() {
        return this.mean;
    }

    public float getAccuracy() {
        return this.accuracy;
    }

    public void fit(DataSet trainingSet) {
        if (trainingSet.rows().size() == 0 || this.mean < 1e-4) {
            return;
        }

        final float sumAll = trainingSet.rows().size();

        for (int n = 0; n < 10; n++) {
            float sumAccu = 0.0f;
            float sumMean = 0.0f;

            this.optimizer.zeroGradients();

            for(DataRow data: trainingSet.rows()) {
                Vector input = data.featuresAsOneVector();
                Vector target = data.label();

                Layer output = this.model.model(input);
                Loss loss = this.criterion.loss(output, target);

                if (output.detach().argmax(0) != target.argmax()) {
                    loss.backward();
                } else {
                    sumAccu++;
                }

                sumMean += loss.getValue().flatten(0);

                if (Float.isNaN(sumMean)) {
                    sumMean = 0.0f;
                }
            }

            this.optimizer.step();

            this.accuracy = Scalar.constrain(sumAccu / sumAll, 0, 1);
            this.mean = Scalar.constrain(sumMean / sumAll, 0, 1);
        }
    }

    public String predict(DataRow row) {
        Vector input = row.featuresAsOneVector();
        Vector output = this.model.model(input).detachAsVector();

        int tagIndex = output.argmax();
        if (tagIndex >= this.tags.size()) {
            tagIndex = 0;
        }
        return this.tags.get(tagIndex);
    }

    private void buildModel() {
        final int inputCount = this.entities.getVectorSize() + 2 * this.ngrams.getVectorSize();
        final int hiddenCount = inputCount / 2;
        final int outputCount = this.tags.getVectorSize();

        final Layer layer1 = new LayerBuilder()
                .setInputUnits(inputCount)
                .setUnits(hiddenCount)
                .setActivation(new LeakyRelu())
                .setNormalizer(new BatchNormalizer())
                .build();

        final Layer layer2 = new LayerBuilder()
                .setInputUnits(hiddenCount)
                .setUnits(outputCount)
                .setActivation(new Softmax())
                .build();

        this.model = new Model().add(layer1).add(layer2);

        this.optimizer = new OptimizerAdamBuilder().build(this.model);

        this.criterion = new Loss(new SoftmaxCrossEntropy());
    }
}
