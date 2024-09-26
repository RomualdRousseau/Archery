package com.github.romualdrousseau.any2json.commons.cv;

public class Template {

    public Template(float[][] data) {
        this.data = data;
        this.inv_area = 1.0f / Float.valueOf(data[0].length * data.length);
    }

    public int getWidth() {
        return this.data[0].length;
    }

    public int getHeight() {
        return this.data.length;
    }

    public float get(int x, int y) {
        return this.data[y][x];
    }

    public float sobel(ISearchBitmap searchBitmap, int x, int y) {
        int w = (this.data[0].length - 1) / 2;
        int h = (this.data.length - 1) / 2;
        float acc = 0;
        for (int i = 0; i < this.data.length; i++) {
            for (int j = 0; j < this.data[i].length; j++) {
                acc += this.data[i][j] * Float.valueOf(searchBitmap.get(x - w + j, y - h + i));
            }
        }
        return acc;
    }

    public float sad(ISearchBitmap searchBitmap, int x, int y) {
        int hw = this.data[0].length / 2;
        int hh = this.data.length / 2;
        float acc = 0.0f;
        for (int i = 0; i < this.data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                float searchPixel = Float.valueOf(searchBitmap.get(x + j - hw, y + i - hh));
                float templatePixel = this.data[i][j];
                acc += Math.abs(searchPixel - templatePixel);
            }
        }
        return acc;
    }

    public float normalize(float v) {
        return 1.0f - v * this.inv_area;
    }

    private float[][] data;
    private float inv_area;
}
