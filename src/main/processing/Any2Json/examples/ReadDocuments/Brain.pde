import com.github.romualdrousseau.shuju.math.Matrix;
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
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.features.VectorFeature;

DataSet TrainingSet = new DataSet();

class Brain_ {
  Model model;
  Optimizer optimizer;
  Loss criterion;
  float accuracy;
  float mean;
  boolean dataChanged;
  
  Brain_() {
    this.accuracy = 0.0;
    this.mean = 1.0;
  }

  void init() {
    this.model = new Model();
    
    this.model.add(new LayerBuilder()
      .setInputUnits(ENTITYVEC_LENGTH + 2 * WORDVEC_LENGTH)
      .setUnits((ENTITYVEC_LENGTH + 2 * WORDVEC_LENGTH) / 2)
      .setActivation(new LeakyRelu())
      .setNormalizer(new BatchNormalizer())
      .build());
      
    this.model.add(new LayerBuilder()
      .setInputUnits((ENTITYVEC_LENGTH + 2 * WORDVEC_LENGTH) / 2)
      .setUnits(TAGVEC_LENGTH)
      .setActivation(new Softmax())
      .build());
    
    this.optimizer = new OptimizerAdamBuilder().build(this.model);

    this.criterion = new Loss(new SoftmaxCrossEntropy());
  }

  Tag predict(Header header, Header[] conflicts) {
    
    DataRow row  = NlpHelper.buildRow(header, conflicts);
    
    Matrix input = new Matrix(row.features().get(0).toVector());
    int tagIndex = this.model.model(input).detach().argmax(0);
    
    Tag[] tags = Tag.values();
    if(tagIndex >= tags.length) {
      tagIndex = 0;
    }
    return tags[tagIndex];
  }

  void fit() {
    if (TrainingSet.rows().size() == 0 || this.mean < 1e-4) {
      return;
    }

    for (int n = 0; n < BRAIN_CLOCK; n++) {
      float sumAccu = 0.0;
      float sumMean = 0.0;

      this.optimizer.zeroGradients();

      for (int i = 0; i < TrainingSet.rows().size(); i++) {
        Matrix input = TrainingSet.rows().get(i).features().get(0).toVector().toMatrix();
        Matrix target = TrainingSet.rows().get(i).getLabel().toVector().toMatrix();

        Layer output = this.model.model(input);
        Loss loss = this.criterion.loss(output, target);
        
        if(output.detach().argmax(0) != target.argmax(0)) {
          loss.backward();
        } else {
          sumAccu++;
        }

        sumMean += loss.getValue().flatten(0);

        if (Float.isNaN(sumMean)) {
          sumMean = 0.0;
          println(loss.getValue());
          println(target);
          println(output.detach());
        }
      }

      this.optimizer.step();

      this.accuracy = constrain(sumAccu / TrainingSet.rows().size(), 0, 1);
      this.mean = constrain(sumMean / TrainingSet.rows().size(), 0, 1);
    }
  }
}
Brain_ Brain = new Brain_();
