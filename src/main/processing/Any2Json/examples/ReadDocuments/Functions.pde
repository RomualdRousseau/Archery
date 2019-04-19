class Linear_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    return input;
  }

  Matrix derivate(Matrix output) {
    return output.copy().ones();
  }
}
Linear_ Linear = new Linear_();

class Relu_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float x, int row, int col, Matrix matrix) {
        return (x <= 0.0) ? 0.0 : x;
      }
    };
    return input.map(fn);
  }

  Matrix derivate(Matrix output) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix matrix) {
        return (y <= 0.0) ? 0.0 : 1.0;
      }
    };
    return output.copy().map(fn);
  }
}
Relu_ Relu = new Relu_();

class LeakyRelu_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float x, int row, int col, Matrix matrix) {
        return (x <= 0.0) ? 0.01 * x : x;
      }
    };
    return input.map(fn);
  }

  Matrix derivate(Matrix output) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix matrix) {
        return (y <= 0.0) ? 0.01 : 1.0;
      }
    };
    return output.copy().map(fn);
  }
}
LeakyRelu_ LeakyRelu = new LeakyRelu_();

class Sigmoid_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float x, int row, int col, Matrix matrix) {
        return 1.0 / (1.0 + exp(-x));
      }
    };
    return input.map(fn);
  }

  Matrix derivate(Matrix output) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix matrix) {
        return y * (1.0 - y);
      }
    };
    return output.copy().map(fn);
  }
}
Sigmoid_ Sigmoid = new Sigmoid_();

class Tanh_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float x, int row, int col, Matrix matrix) {
        return tanh(x);
      }
    };
    return input.map(fn);
  }

  Matrix derivate(Matrix output) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix matrix) {
        return 1.0 - y * y;
      }
    };
    return output.copy().map(fn);
  }
}
Tanh_ Tanh = new Tanh_();

class Softmax_ implements ActivationFunc {
  Matrix apply(Matrix input) {
    final float c = -input.get(input.argmax(0), 0);

    float temp = 0.0;
    for (int k = 0; k < input.rows; k++) {
      temp += exp(input.get(k, 0) + c);
    } 
    final float sum = temp;

    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float x, int row, int col, Matrix matrix) {
        return exp(x + c) / sum;
      }
    };

    return input.map(fn);
  }

  Matrix derivate(Matrix output) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix output) {
        final float k = (row == col) ? 1.0 : 0.0;
        return output.get(col, 0) * (k - output.get(row, 0));
      }
    };
    return new Matrix(output.rows, output.rows).map(fn, output);
  }
}
Softmax_ Softmax = new Softmax_();

class MeanSquaredError_ implements LossFunc {
  Matrix apply(Matrix output, Matrix target) {
    return target.copy().sub(output).pow(2.0).mult(0.5);
  }

  Matrix derivate(Matrix output, Matrix target) {
    return output.copy().sub(target);
  }
}
MeanSquaredError_ MeanSquaredError = new MeanSquaredError_();

class Huber_ implements LossFunc {
  final float alpha = 1.0;
  
  Matrix apply(Matrix output, Matrix target) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix output) {
        float a = y - output.get(row, col);
        if (abs(a) <= alpha) {
          return 0.5 * a * a;
        } else {
          return alpha * (abs(a) - 0.5 * alpha);
        }
      }
    };
    return target.copy().map(fn, output);
  }

  Matrix derivate(Matrix output, Matrix target) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix target) {
        float a = y - target.get(row, col);
        if (a < -alpha) {
          return -alpha;
        } else if (a <= alpha) {
          return a;
        } else {
          return alpha;
        }
      }
    };
    return output.copy().map(fn, target);
  }
}
Huber_ Huber = new Huber_();

class SoftmaxCrossEntropy_ implements LossFunc {
  Matrix apply(Matrix output, final Matrix target) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix output) {
        float a = output.get(row, col);
        return (a > 0.0) ? -y * log(a) : 0.0;
      }
    }; 
    return target.copy().map(fn, output);
  }

  Matrix derivate(Matrix output, Matrix target) {
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float y, int row, int col, Matrix output) {
        float a = output.get(row, col);
        return (a > 0.0) ? -y / a : 0.0;
      }
    };
    return target.copy().map(fn, output);
  }
}
SoftmaxCrossEntropy_ SoftmaxCrossEntropy = new SoftmaxCrossEntropy_();

class GlorotUniformInitializer_ implements InitializerFunc {
  void apply(Matrix matrix) {
    matrix.randomize(sqrt(6.0 / sqrt(matrix.rows + matrix.cols)));
  }
}
GlorotUniformInitializer_ GlorotUniformInitializer = new GlorotUniformInitializer_();

class LecunUniformInitializer_ implements InitializerFunc {
  void apply(Matrix matrix) {
    matrix.randomize(sqrt(6.0 / sqrt(matrix.rows)));
  }
}
LecunUniformInitializer_ LecunUniformInitializer = new LecunUniformInitializer_();

class HeUniformInitializer_ implements InitializerFunc {
  void apply(Matrix matrix) {
    matrix.randomize(sqrt(3.0 / sqrt(matrix.rows)));
  }
}
HeUniformInitializer_ HeUniformInitializer = new HeUniformInitializer_();

class L2Normalizer_ implements NormalizerFunc {
  void apply(Matrix matrix) {
    matrix.l2Norm();
  }
}
L2Normalizer_ L2Normalizer = new L2Normalizer_();

class BatchNormalizer_ implements NormalizerFunc {
  void apply(Matrix matrix) {
    matrix.batchNorm(1.0, 0.0);
  }
}
BatchNormalizer_ BatchNormalizer = new BatchNormalizer_();

class ExponentialScheduler implements LearningRateScheduler {
  float decay;
  int step;
  float minRate;

  public ExponentialScheduler(float decay, int step, float minRate) {
    this.decay = decay;
    this.step = step;
    this.minRate = minRate;
  }

  public void apply(Optimizer optimizer) {
    int a = optimizer.epoch / this.step;
    optimizer.learningRate = max(this.minRate, optimizer.learningRate0 * exp(-this.decay * a));
  }
}

class OptimizerSgd extends Optimizer {
  float momemtum;
  
  OptimizerSgd(Model model, float learningRate, LearningRateScheduler scheduler) {
    super(model, learningRate, scheduler);
    this.momemtum = 0.9;
  }

  Matrix computeGradients(Parameters p) {
    final float lr = this.learningRate;
    p.M.mult(this.momemtum).fma(p.G, 1.0 - this.momemtum); 
    return p.M.copy().mult(lr);
  }
}

class OptimizerRMSProp extends Optimizer {
  float b;

  OptimizerRMSProp(Model model, float learningRate, LearningRateScheduler scheduler, float b) {
    super(model, learningRate, scheduler);
    this.b = b;
  }

  Matrix computeGradients(Parameters p) {
    final float lr = this.learningRate;
    
    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float m, int row, int col, Matrix cache) {
        float v = cache.get(row, col);
        return lr * m / (sqrt(v) + EPSILON);
      }
    };

    p.V.mult(this.b).fma(p.G.copy().pow(2.0), 1.0 - this.b);

    return p.G.copy().map(fn, p.V);
  }
}

class OptimizerAdam extends Optimizer {
  float b1;
  float b2;

  OptimizerAdam(Model model, float learningRate, LearningRateScheduler scheduler, float b1, float b2) {
    super(model, learningRate, scheduler);
    this.b1 = b1;
    this.b2 = b2;
  }

  Matrix computeGradients(Parameters p) {
    final float lr = this.learningRate * sqrt(1.0 - pow(this.b2, this.epoch)) / (1.0 - pow(this.b1, this.epoch));

    final MatrixFunction<Float, Float> fn = new MatrixFunction<Float, Float>() {
      public final Float apply(Float m, int row, int col, Matrix cache) {
        float v = cache.get(row, col);
        return lr * m / (sqrt(v) + EPSILON);
      }
    };

    p.M.mult(this.b1).fma(p.G, 1.0 - this.b1);
    p.V.mult(this.b2).fma(p.G.copy().pow(2.0), 1.0 - this.b2);

    return p.M.copy().map(fn, p.V);
  }
}
