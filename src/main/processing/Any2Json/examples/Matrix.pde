static final float EPSILON = 1e-8;

interface MatrixFunction<T, R> {
  R apply(T v, int row, int col, Matrix matrix);
}

class Matrix {
  int rows;
  int cols;
  float[][] data;

  public Matrix(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.data = new float[this.rows][this.cols];
  }

  public Matrix(int rows, int cols, float v) {
    this.rows = rows;
    this.cols = cols;
    this.data = new float[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = v;
      }
    }
  }

  public Matrix(float[] v) {
    this.rows = v.length;
    this.cols = 1;
    this.data = new float[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++) {
      this.data[i][0] = v[i];
    }
  }

  public Matrix(Float[] v) {
    this.rows = v.length;
    this.cols = 1;
    this.data = new float[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++) {
      this.data[i][0] = v[i];
    }
  }

  public Matrix(JSONObject json) {
    this.rows = json.getInt("rows");
    this.cols = json.getInt("cols");
    this.data = new float[this.rows][this.cols];
    JSONArray jsonData = json.getJSONArray("data");
    for (int i = 0; i < this.rows; i++) {
      JSONArray jsonRow = jsonData.getJSONArray(i);
      for (int j = 0; j < this.cols; j++) {
        this.data[i][j] = jsonRow.getFloat(j);
      }
    }
  }

  public float get(int row, int col) {
    return this.data[row][col];
  }

  public void set(int row, int col, float v) {
    this.data[row][col] = v;
  }

  public boolean equals(Matrix m) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    boolean result = true;
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        result &= a[j] == b[j];
      }
    }
    return result;
  }

  public float sparsity() {
    int count = 0;
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        count += (a[j] == 0.0) ? 1 : 0;
      }
    }
    return float(count) / float(this.rows * this.cols);
  }

  public float min(int col) {
    float minValue = this.data[0][col];
    for (int i = 1; i < this.rows; i++) {
      if (this.data[i][col] < minValue) {
        minValue = this.data[i][col];
      }
    }
    return minValue;
  }

  public float max(int col) {
    float maxValue = this.data[0][col];
    for (int i = 1; i < this.rows; i++) {
      if (this.data[i][col] > maxValue) {
        maxValue = this.data[i][col];
      }
    }
    return maxValue;
  }

  public int argmax(int col) {
    int result = 0;
    float maxValue = this.data[0][col];
    for (int i = 1; i < this.rows; i++) {
      if (this.data[i][col] > maxValue) {
        maxValue = this.data[i][col];
        result = i;
      }
    }
    return result;
  }

  public float flatten(int col) {
    float sum = 0.0;
    for (int i = 0; i < this.rows; i++) {
      sum += this.data[i][col];
    }
    return sum;
  }

  public Matrix zero() {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = 0.0;
      }
    }
    return this;
  }

  public Matrix ones() {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = 1.0;
      }
    }
    return this;
  }

  public Matrix identity() {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = (i == j) ? 1.0 : 0.0;
      }
    }
    return this;
  }

  public Matrix mutate(float rate, float variance) {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        if (random(1.0) < rate) {
          a[j] += randomGaussian() * variance;
        }
      }
    }
    return this;
  }

  public Matrix randomize() {
    this.randomize(1.0);
    return this;
  }

  public Matrix randomize(float n) {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = random(-n, n);
      }
    }
    return this;
  }

  public Matrix l2Norm() {
    for (int j = 0; j < this.cols; j++) {
      float sum = 0.0;
      for (int i = 0; i < this.rows; i++) {
        sum += this.data[i][j] * this.data[i][j];
      }
      sum = sqrt(sum);

      for (int i = 0; i < this.rows; i++) {
        this.data[i][j] /= sum;
      }
    }
    return this;
  }

  public Matrix batchNorm(float a, float b) {
    for (int j = 0; j < this.cols; j++) {
      float avg = 0.0;
      for (int i = 0; i < this.rows; i++) {
        avg += this.data[i][j];
      }
      avg /= (float) this.rows;

      float var = 0.0;
      for (int i = 0; i < this.rows; i++) {
        float x = (this.data[i][j] - avg);
        var += x * x;
      }
      var /= (float) this.rows;

      for (int i = 0; i < this.rows; i++) {
        float x = (this.data[i][j] - avg) / sqrt(var + EPSILON);
        this.data[i][j] = a * x + b;
      }
    }
    return this;
  }

  public Matrix add(final Matrix m) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] += b[j];
      }
    }
    return this;
  }

  public Matrix sub(final Matrix m) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] -= b[j];
      }
    }
    return this;
  }

  public Matrix mult(float n) {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] *= n;
      }
    }
    return this;
  }

  public Matrix mult(final Matrix m) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] *= b[j];
      }
    }
    return this;
  }

  public Matrix div(float n) {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] /= n;
      }
    }
    return this;
  }

  public Matrix div(final Matrix m) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] /= b[j];
      }
    }
    return this;
  }

  public Matrix pow(float n) {
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = (float) Math.pow(a[j], n);
      }
    }
    return this;
  }

  public Matrix fma(final Matrix m1, final Matrix m2) {
    if (this.rows != m1.rows || this.cols != m2.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B * C.");
    }
    if (m1.cols != m2.rows) {
      throw new IllegalArgumentException("cols of B must match rows of C.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] c = this.data[i];
      for (int k = 0; k < m1.cols; k++) {
        float a = m1.data[i][k];
        float[] b = m2.data[k];
        for (int j = 0; j < this.cols; j++) {
          c[j] = a * b[j] + c[j];
        }
      }
    }
    return this;
  }

  public Matrix fma(final Matrix m1, final Matrix m2, boolean transposeA, boolean transposeB) {
    final int colsA = transposeA ? m1.rows : m1.cols;
    final int rowsA = transposeA ? m1.cols : m1.rows;
    final int colsB = transposeB ? m2.rows : m2.cols;
    final int rowsB = transposeB ? m2.cols : m2.rows;

    if (this.rows != rowsA || this.cols != colsB) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B * C.");
    }
    if (colsA != rowsB) {
      throw new IllegalArgumentException("cols of B must match rows of C.");
    }

    if (transposeA && transposeB) {
      for (int i = 0; i < this.rows; i++) {
        float[] c = this.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = m1.data[k][i];
          float[][] b = m2.data;
          for (int j = 0; j < this.cols; j++) {
            c[j] = a * b[j][k] + c[j];
          }
        }
      }
    } else if (transposeA && !transposeB) {
      for (int i = 0; i < this.rows; i++) {
        float[] c = this.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = m1.data[k][i];
          float[] b = m2.data[k];
          for (int j = 0; j < this.cols; j++) {
            c[j] = a * b[j] + c[j];
          }
        }
      }
    } else if (!transposeA && transposeB) {
      for (int i = 0; i < this.rows; i++) {
        float[] c = this.data[i];
        float[] a = m1.data[i];
        for (int j = 0; j < this.cols; j++) {
          float[] b = m2.data[j];
          for (int k = 0; k < colsA; k++) {
            c[j] = a[k] * b[k] + c[j];
          }
        }
      }
    } else {
      for (int i = 0; i < this.rows; i++) {
        float[] c = this.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = m1.data[i][k];
          float[] b = m2.data[k];
          for (int j = 0; j < this.cols; j++) {
            c[j] = a * b[j] + c[j];
          }
        }
      }
    }
    return this;
  }

  public Matrix fma(final Matrix m, float n) {
    if (this.rows != m.rows || this.cols != m.cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      float[] b = m.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = b[j] * n + a[j];
      }
    }
    return this;
  }

  public Matrix fma(final Matrix m, float n, boolean transpose) {
    final int cols = transpose ? m.rows : m.cols;
    final int rows = transpose ? m.cols : m.rows;

    if (this.rows != rows || this.cols != cols) {
      throw new IllegalArgumentException("cardinality of A must match cardinality of B.");
    }
    if (transpose) {
      for (int i = 0; i < this.rows; i++) {
        float[] a = this.data[i];
        float[] b = m.data[i];
        for (int j = 0; j < this.cols; j++) {
          a[j] = b[i] * n + a[j];
        }
      }
    } else {
      for (int i = 0; i < this.rows; i++) {
        float[] a = this.data[i];
        float[] b = m.data[i];
        for (int j = 0; j < this.cols; j++) {
          a[j] = b[j] * n + a[j];
        }
      }
    }
    return this;
  }

  public Matrix map(MatrixFunction<Float, Float> fn) {
    if (fn == null) {
      throw new IllegalArgumentException("function is not defined");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = fn.apply(a[j], i, j, this);
      }
    }
    return this;
  }

  public Matrix map(MatrixFunction<Float, Float> fn, Matrix other) {
    if (fn == null) {
      throw new IllegalArgumentException("function is not defined");
    }
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      for (int j = 0; j < this.cols; j++) {
        a[j] = fn.apply(a[j], i, j, other);
      }
    }
    return this;
  }

  public Matrix copy() {
    Matrix result = new Matrix(this.rows, this.cols);
    for (int i = 0; i < result.rows; i++) {
      float[] a = this.data[i];
      float[] b = result.data[i];
      System.arraycopy(a, 0, b, 0, result.cols);
      //for (int j = 0; j < result.cols; j++) {
      //  b[j] = a[j];
      //}
    }
    return result;
  }

  public Matrix squarify(boolean diagonal) {
    Matrix result = new Matrix(this.rows, this.rows);
    if (this.rows == this.cols) {
      for (int i = 0; i < this.rows; i++) {
        float[] a = this.data[i];
        float[] b = result.data[i];
        for (int j = 0; j < this.cols; j++) {
          b[j] = a[j];
        }
      }
    } else if (diagonal) {
      for (int i = 0; i < this.rows; i++) {
        result.data[i][i] = this.data[i][0];
      }
    } else {
      for (int i = 0; i < this.rows; i++) {
        float[] a = this.data[i];
        float[] b = result.data[i];
        for (int j = 0; j < this.rows; j++) {
          b[j] = a[0];
        }
      }
    }
    return result;
  }

  public Matrix transpose() {
    Matrix result = new Matrix(this.cols, this.rows);
    for (int i = 0; i < result.rows; i++) {
      float[] a = this.data[i];
      float[] b = result.data[i];
      for (int j = 0; j < result.cols; j++) {
        b[j] = a[i];
      }
    }
    return result;
  }

  public Matrix transform(final Matrix m) {
    if (this.cols != m.rows) {
      throw new IllegalArgumentException("cols of A must match rows of B.");
    }
    Matrix result = new Matrix(this.rows, m.cols, 0.0);
    for (int i = 0; i < result.rows; i++) {
      float[] c = result.data[i];
      for (int k = 0; k < this.cols; k++) {
        float a = this.data[i][k];
        float[] b = m.data[k];
        for (int j = 0; j < result.cols; j++) {
          c[j] = a * b[j] + c[j];
        }
      }
    }
    return result;
  }

  public Matrix transform(final Matrix m, boolean transposeA, boolean transposeB) {
    final int colsA = transposeA ? this.rows : this.cols;
    final int rowsA = transposeA ? this.cols : this.rows;
    final int colsB = transposeB ? m.rows : m.cols;
    final int rowsB = transposeB ? m.cols : m.rows;
    
    if (colsA != rowsB) {
      throw new IllegalArgumentException("rows of A must match rows of B.");
    }

    Matrix result = new Matrix(rowsA, colsB, 0.0);
    if (transposeA && transposeB) {
      for (int i = 0; i < result.rows; i++) {
        float[] c = result.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = this.data[k][i];
          float[][] b = m.data;
          for (int j = 0; j < result.cols; j++) {
            c[j] = a * b[j][k] + c[j];
          }
        }
      }
    } else if (transposeA && !transposeB) {
      for (int i = 0; i < result.rows; i++) {
        float[] c = result.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = this.data[k][i];
          float[] b = m.data[k];
          for (int j = 0; j < result.cols; j++) {
            c[j] = a * b[j] + c[j];
          }
        }
      }
    } else if (!transposeA && transposeB) {
      for (int i = 0; i < result.rows; i++) {
        float[] c = result.data[i];
        float[] a = this.data[i];
        for (int j = 0; j < result.cols; j++) {
          float[] b = m.data[j];
          for (int k = 0; k < colsA; k++) {
            c[j] = a[k] * b[k] + c[j];
          }
        }
      }
    } else {
      for (int i = 0; i < result.rows; i++) {
        float[] c = result.data[i];
        for (int k = 0; k < colsA; k++) {
          float a = this.data[i][k];
          float[] b = m.data[k];
          for (int j = 0; j < result.cols; j++) {
            c[j] = a * b[j] + c[j];
          }
        }
      }
    }

    return result;
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();

    if(this.rows == 0 || this.cols == 0) {
      return json;
    }

    JSONArray jsonData = new JSONArray();
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      JSONArray jsonRow = new JSONArray();
      for (int j = 0; j < this.cols; j++) {
        jsonRow.setFloat(j, a[j]);
      }
      jsonData.setJSONArray(i, jsonRow);
    }
    
    json.setInt("rows", this.rows);
    json.setInt("cols", this.cols);
    json.setJSONArray("data", jsonData);
    return json;
  }

  public String toString() {
    if(this.rows == 0 || this.cols == 0) {
      return "||";
    }
    
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < this.rows; i++) {
      float[] a = this.data[i];
      result.append("| ");
      for (int j = 0; j < this.cols - 1; j++) {
        result.append(String.format("%.5f\t", a[j]));
      }
      result.append(String.format("%.5f", a[this.cols - 1]));
      result.append(" |").append(System.lineSeparator());
    }
    
    return result.toString();
  }
}
