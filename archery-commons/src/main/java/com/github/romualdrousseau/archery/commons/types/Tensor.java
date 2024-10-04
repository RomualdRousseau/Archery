package com.github.romualdrousseau.archery.commons.types;

public class Tensor {

    public static final Tensor Null = Tensor.zeros(0);

    public static Tensor of(final double... data) {
        final var floats = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            floats[i] = (float) data[i];
        }
        return new Tensor(floats);
    }

    public static Tensor of(final float... data) {
        return new Tensor(data);
    }

    public static Tensor zeros(final int size) {
        final var zeros = new float[size];
        for (int i = 0; i < size; i++) {
            zeros[i] = 0.0f;
        }
        return new Tensor(zeros);
    }

    public final int size;
    public final float[] data;

    public Tensor(final float[] data) {
        this.data = data;
        this.size = data.length;
    }

    public Tensor iadd(final Tensor t) {
        assert this.size == t.size;
        for (int i = 0; i < this.size; i++) {
            this.data[i] += t.data[i];
        }
        return this;
    }

    public Tensor if_lt_then(final float n, final float f, final float g) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (this.data[i] < n) ? f : g;
        }
        return this;
    }

    public int argmax() {
        if (this.size < 1) {
            return -1;
        }
        int i_max = 0;
        float v_max = this.data[0];
        for (int i = 1; i < this.data.length; i++) {
            if (this.data[i] > v_max) {
                i_max = i;
                v_max = this.data[i];
            }
        }
        return i_max;
    }

    public boolean equals(Tensor other, float eps) {
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < this.data.length; i++) {
            if (Math.abs(this.data[i] - other.data[i]) > eps) {
                return false;
            }
        }
        return true;
    }
}
