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
      .setActivation(LeakyRelu)
      .setNormalizer(BatchNormalizer)
      .build());
      
    this.model.add(new LayerBuilder()
      .setInputUnits((ENTITYVEC_LENGTH + 2 * WORDVEC_LENGTH) / 2)
      .setUnits(TAGVEC_LENGTH)
      .setActivation(Softmax)
      .build());
    
    this.optimizer = new OptimizerAdamBuilder().build(this.model);

    this.criterion = new Loss(SoftmaxCrossEntropy);
  }

  Tag predict(Header header, Header[] conflicts) {
    Matrix input = new Matrix(TrainingSet.buildInput(header, conflicts));
    int i = this.model.model(input).detach().argmax(0);
    
    Tag[] tags = Tag.values();
    if(i >= tags.length) {
      i = 0;
    }
    return tags[i];
  }

  void fit() {
    if (TrainingSet.size() == 0 || this.mean < 1e-4) {
      return;
    }

    for (int n = 0; n < BRAIN_CLOCK; n++) {
      float sumAccu = 0.0;
      float sumMean = 0.0;

      this.optimizer.zeroGradients();

      for (int i = 0; i < TrainingSet.size(); i++) {
        Matrix input = new Matrix(TrainingSet.inputs.get(i));
        Matrix target = new Matrix(TrainingSet.targets.get(i));

        Layer output = this.model.model(input);
        Loss loss = this.criterion.loss(output, target);
        
        if(output.output.argmax(0) != target.argmax(0)) {
          loss.backward();
        } else {
          sumAccu++;
        }

        sumMean += loss.value.flatten(0);

        if (Float.isNaN(sumMean)) {
          sumMean = 0.0;
          println(loss.value);
          println(target);
          println(output.detach());
        }
      }

      this.optimizer.step();

      this.accuracy = constrain(sumAccu / TrainingSet.size(), 0, 1);
      this.mean = constrain(sumMean / TrainingSet.size(), 0, 1);
    }
  }
}
Brain_ Brain = new Brain_();
