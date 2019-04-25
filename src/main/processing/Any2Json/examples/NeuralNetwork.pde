interface ActivationFunc {
  Matrix apply(Matrix x);
  Matrix derivate(Matrix y);
}

interface LossFunc {
  Matrix apply(Matrix output, Matrix target);
  Matrix derivate(Matrix output, Matrix target);
}

interface InitializerFunc {
  void apply(Matrix m);
}

interface NormalizerFunc {
  void apply(Matrix m);
}

interface LearningRateScheduler {
  void apply(Optimizer optimizer);
}

class Parameters {
  Matrix W, G, M, V;

  Parameters(int units) {
    this(1, units);
  }

  Parameters(int inputUnits, int units) {
    this.W = new Matrix(units, inputUnits, 0.0);
    this.G = W.copy().zero();
    this.M = W.copy().zero();
    this.V = W.copy().zero();
  }

  void reset() {
    this.W.zero();
    this.G.zero();
    this.M.zero();
    this.V.zero();
  }

  void fromJSON(JSONObject json) {
    this.W = new Matrix(json);
    this.G = W.copy().zero();
    this.M = W.copy().zero();
    this.V = W.copy().zero();
  }

  JSONObject toJSON() {
    return this.W.toJSON();
  }
}

class Layer {
  Parameters weights;
  Parameters biases;
  float bias;

  ActivationFunc activation;
  InitializerFunc initializer;
  NormalizerFunc normalizer;

  Matrix output;

  Layer prev;
  Layer next;

  Layer(int inputUnits, int units, float bias, ActivationFunc activation, InitializerFunc initializer, NormalizerFunc normalizer) {
    this.weights = new Parameters(inputUnits, units);
    this.biases = new Parameters(units);
    this.bias = bias;

    this.activation = (activation == null) ? Linear : activation;
    this.initializer = (initializer == null) ? GlorotUniformInitializer : initializer;
    this.normalizer = normalizer;

    this.output = null;

    this.prev = null;
    this.next = null;
    this.reset();
  }

  void reset() {
    this.weights.reset();
    this.biases.reset();
    this.initializer.apply(this.weights.W);
  }

  void adjustGradients(Parameters p, Matrix g) {
    p.W.sub(g);
    if (this.normalizer != null) {
      this.normalizer.apply(p.W);
    }
  }

  Matrix detach() {
    return this.output;
  }

  void fromJSON(JSONObject json) {
    this.weights.fromJSON(json.getJSONObject("weights"));
    this.biases.fromJSON(json.getJSONObject("biases"));
    this.bias = json.getFloat("bias");
  }

  JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.setJSONObject("weights", this.weights.toJSON());
    json.setJSONObject("biases", this.biases.toJSON());
    json.setFloat("bias", this.bias);
    return json;
  }
}

class Model {
  Layer start;
  Layer end;

  Model() {
    this.start = new LayerBuilder().build();
    this.end = this.start;
  }

  void reset() {
    for (Layer layer = this.start.next; layer != null; layer = layer.next) {
      layer.reset();
    }
  }

  void add(Layer layer) {
    layer.prev = this.end;  
    this.end.next = layer;
    this.end = layer;
  }

  Layer model(Matrix input) {
    this.start.output = input;
    for (Layer layer = this.start.next; layer != null; layer = layer.next) {
      Matrix net = xw_plus_b(layer.prev.output, layer.weights.W, layer.biases.W); 
      layer.output = layer.activation.apply(net);
    }
    return this.end;
  }

  void fromJSON(JSONArray json) {
    int i = json.size();
    for (Layer layer = this.start.next; layer != null; layer = layer.next, i--);
    if (i != 0) {
      throw new IllegalArgumentException("model must match the model layout.");
    }
    for (Layer layer = this.start.next; layer != null; layer = layer.next, i++) {
      layer.fromJSON(json.getJSONObject(i));
    }
  }

  JSONArray toJSON() {
    JSONArray json = new JSONArray();
    for (Layer layer = this.start.next; layer != null; layer = layer.next) {
      json.append(layer.toJSON());
    }
    return json;
  }
}

class Loss {
  LossFunc lossFunc;
  Matrix value;
  Matrix rate;
  Layer output;

  Loss(LossFunc lossFunc) {
    this.lossFunc = lossFunc;
  }

  Loss loss(Layer output, Matrix target) {
    this.value = this.lossFunc.apply(output.output, target);
    this.rate = this.lossFunc.derivate(output.output, target);
    this.output = output;
    return this;
  }

  Loss backward() {
    Matrix error = this.rate;
    for (Layer layer = this.output; layer.prev != null; layer = layer.prev) {
      error = a_mul_b(error, layer.activation.derivate(layer.output));
      layer.weights.G.fma(error, layer.prev.output, false, true);
      layer.biases.G.fma(error, layer.prev.bias);
      error = layer.weights.W.transform(error, true, false);
    }
    return this;
  }
}

abstract class Optimizer {
  Model model;
  LearningRateScheduler scheduler;
  float learningRate0, learningRate;
  int epoch;

  Optimizer(Model model, float learningRate, LearningRateScheduler scheduler) {
    this.model = model;
    this.learningRate0 = learningRate;
    this.learningRate = learningRate;
    this.epoch = 1;
    this.scheduler = scheduler;
  }

  void reset() {
    this.learningRate = this.learningRate0;
    this.epoch = 1;
    for (Layer layer = this.model.start.next; layer != null; layer = layer.next) {
      layer.weights.M.zero();
      layer.weights.V.zero();
      layer.biases.M.zero();
      layer.biases.V.zero();
    }
  }

  void zeroGradients() {
    for (Layer layer = this.model.start.next; layer != null; layer = layer.next) {
      layer.weights.G.zero();
      layer.biases.G.zero();
    }
  }

  void step() {
    for (Layer layer = this.model.start.next; layer != null; layer = layer.next) {
      layer.adjustGradients(layer.weights, this.computeGradients(layer.weights));
      layer.adjustGradients(layer.biases, this.computeGradients(layer.biases));
    }

    this.epoch++;

    if (this.scheduler != null) {
      this.scheduler.apply(this);
    }
  }

  abstract Matrix computeGradients(Parameters p);
}

abstract class OptimizerBuilder<T extends Optimizer> {
  float learningRate;
  LearningRateScheduler scheduler;
  
  OptimizerBuilder() {
    this.learningRate = 0.001;
    this.scheduler = null;
  }
  
  OptimizerBuilder setLearningRate(float learningRate) {
    this.learningRate = learningRate;
    return this;
  }

  OptimizerBuilder setScheduler(LearningRateScheduler scheduler) {
    this.scheduler = scheduler;
    return this;
  }

  abstract T build(Model model);
}
