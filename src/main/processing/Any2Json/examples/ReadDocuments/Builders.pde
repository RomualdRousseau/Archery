class LayerBuilder {
  int inputUnits;
  int units;
  float bias;
  ActivationFunc activation;
  InitializerFunc initializer;
  NormalizerFunc normalizer;
  
  LayerBuilder() {
    this.inputUnits = 0;
    this.units = 0;
    this.bias = 1.0;
    this.activation = Linear;
    this.initializer = GlorotUniformInitializer;
    this.normalizer = null;
  }

  Layer build() {
    return new Layer(this.inputUnits, this.units, this.bias, this.activation, this.initializer, this.normalizer);
  }
  
  LayerBuilder setBias(float bias) {
    this.bias = bias;
    return this;
  }

  LayerBuilder setInputUnits(int inputUnits) {
    this.inputUnits = inputUnits;
    return this;
  }
  
  LayerBuilder setUnits(int units) {
    this.units = units;
    return this;
  }
  
  LayerBuilder setActivation(ActivationFunc activation) {
    this.activation = activation;
    return this;
  }
  
  LayerBuilder setInitializer(InitializerFunc initializer) {
    this.initializer = initializer;
    return this;
  }
  
  LayerBuilder setNormalizer(NormalizerFunc normalizer) {
    this.normalizer = normalizer;
    return this;
  }
}

class OptimizerSgdBuilder extends OptimizerBuilder<OptimizerSgd> {
  OptimizerSgdBuilder() {
    super();
  }
  
  OptimizerSgd build(Model model) {
    return new OptimizerSgd(model, this.learningRate, this.scheduler);
  }
}

class OptimizerRMSPropBuilder extends OptimizerBuilder<OptimizerRMSProp> {
  float b;

  OptimizerRMSPropBuilder() {
    super();
    this.b = 0.9;
  }
  
  OptimizerRMSPropBuilder setB(float b) {
    this.b = b;
    return this;
  }
  
  OptimizerRMSProp build(Model model) {
    return new OptimizerRMSProp(model, this.learningRate, this.scheduler, this.b);
  }
}

class OptimizerAdamBuilder extends OptimizerBuilder<OptimizerAdam> {
  float b1;
  float b2;
  
  OptimizerAdamBuilder() {
    super();
    this.b1 = 0.9;
    this.b2 = 0.999;
  }
  
  OptimizerAdamBuilder setB1(float b1) {
    this.b1 = b1;
    return this;
  }
  
  OptimizerAdamBuilder setB2(float b2) {
    this.b2 = b2;
    return this;
  }
  
  OptimizerAdam build(Model model) {
    return new OptimizerAdam(model, this.learningRate, this.scheduler, this.b1, this.b2);
  }
}
