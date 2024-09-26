package com.github.romualdrousseau.any2json.commons.cv;

public class Filter {

    public Filter(Template filter) {
        this.filter = filter;
    }

    public void apply(ISearchBitmap searchBitmap, double threshold) {
        for (int y = 0; y < searchBitmap.getHeight(); y++) {
            for (int x = 0; x < searchBitmap.getWidth(); x++) {
                float acc = this.filter.sobel(searchBitmap, x, y);
                if (acc < threshold) {
                    searchBitmap.set(x, y, 0);
                }
            }
        }
    }

    public void apply(ISearchBitmap searchBitmap, int[] clip, double threshold) {
        for (int y = clip[1]; y < clip[3]; y++) {
            for (int x = clip[0]; x < clip[2]; x++) {
                float acc = this.filter.sobel(searchBitmap, x, y);
                if (acc < threshold) {
                    searchBitmap.set(x, y, 0);
                }
            }
        }
    }

    public void applyNeg(ISearchBitmap searchBitmap, double threshold) {
        for (int y = 0; y < searchBitmap.getHeight(); y++) {
            for (int x = 0; x < searchBitmap.getWidth(); x++) {
                float acc = this.filter.sobel(searchBitmap, x, y);
                if (acc >= threshold) {
                    searchBitmap.set(x, y, 1);
                }
            }
        }
    }

    public void applyNeg(ISearchBitmap searchBitmap, int[] clip, double threshold) {
        for (int y = clip[1]; y < clip[3]; y++) {
            for (int x = clip[0]; x < clip[2]; x++) {
                float acc = this.filter.sobel(searchBitmap, x, y);
                if (acc >= threshold) {
                    searchBitmap.set(x, y, 1);
                }
            }
        }
    }

    public void apply(ISearchBitmap sourceBitmap, ISearchBitmap destBitmap, double threshold) {
        for (int y = 0; y < sourceBitmap.getHeight(); y++) {
            for (int x = 0; x < sourceBitmap.getWidth(); x++) {
                float acc = this.filter.sobel(sourceBitmap, x, y);
                if (acc < threshold) {
                    destBitmap.set(x, y, 0);
                } else {
                    destBitmap.set(x, y, 1);
                }
            }
        }
    }

    public void apply(ISearchBitmap sourceBitmap, ISearchBitmap destBitmap, int[] clip, double threshold) {
        for (int y = clip[1]; y < clip[3]; y++) {
            for (int x = clip[0]; x < clip[2]; x++) {
                float acc = this.filter.sobel(sourceBitmap, x, y);
                if (acc < threshold) {
                    destBitmap.set(x, y, 0);
                } else {
                    destBitmap.set(x, y, 1);
                }
            }
        }
    }

    private Template filter;
}
