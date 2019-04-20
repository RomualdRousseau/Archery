float[] oneHot(int i, int n) {
  float[] state = new float[n];
  state[i] = 1.0;
  return state;
}

float tanh(float x) {
  return (float)Math.tanh(x);
}

float sgn(float x) {
  return (x > 0.0) ? 1.0 : ((x < 0.0) ? -1.0 : 0.0);
}

int argmax(float[] v) {
  int result = 0;
  float maxValue = v[0];
  for (int i = 1; i < v.length; i++) {
    if (v[i] > maxValue) {
      maxValue = v[i];
      result = i;
    }
  }
  return result;
}

int argmin(float[] v) {
  int result = 0;
  float minValue = v[0];
  for (int i = 1; i < v.length; i++) {
    if (v[i] < minValue) {
      minValue = v[i];
      result = i;
    }
  }
  return result;
}

float[] addvf(float[] u, float[] v) {
  for (int i = 0; i < u.length; i++) {
    u[i] += v[i];
  }
  return u;
}

float[] constrainvf(float[] u, float a, float b) {
  for (int i = 0; i < u.length; i++) {
    u[i] = constrain(u[i], a, b);
  }
  return u;
}

float[] filtervf(float[] u, float p, float a, float b) {
  for (int j = 0; j < u.length; j++) {
    u[j] = (u[j] < p) ? a : b;
  }
  return u;
}

float unlerp(float a, float b, float v) {
  return (v - a) / (b - a);
}

Matrix xw_plus_b(Matrix input, Matrix weights, Matrix bias) {
  return weights.transform(input).add(bias);
}

Matrix a_mul_b(Matrix a, Matrix b) {
  return (a.cols == b.cols) ? b.mult(a) : b.transform(a);
}
