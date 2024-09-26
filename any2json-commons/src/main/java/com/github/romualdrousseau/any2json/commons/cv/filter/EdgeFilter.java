package com.github.romualdrousseau.any2json.commons.cv.filter;

import com.github.romualdrousseau.any2json.commons.cv.Filter;
import com.github.romualdrousseau.any2json.commons.cv.ISearchBitmap;
import com.github.romualdrousseau.any2json.commons.cv.Template;

public class EdgeFilter extends Filter {

    public EdgeFilter() {
        super(null);
    }

    public void apply(ISearchBitmap sourceBitmap, ISearchBitmap destBitmap, double threshold) {
        for (int y = 0; y < sourceBitmap.getHeight(); y++) {
            for (int x = 0; x < sourceBitmap.getWidth(); x++) {
                float lx = this.edgeX.sobel(sourceBitmap, x, y);
                float ly = this.edgeY.sobel(sourceBitmap, x, y);
                double acc = Math.sqrt(lx * lx + ly * ly);
                // double phi = Math.atan2(ly, lx);
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
                float lx = this.edgeX.sobel(sourceBitmap, x, y);
                float ly = this.edgeY.sobel(sourceBitmap, x, y);
                double acc = Math.sqrt(lx * lx + ly * ly);
                // double phi = Math.atan2(ly, lx);
                if (acc < threshold) {
                    destBitmap.set(x, y, 0);
                } else {
                    destBitmap.set(x, y, 1);
                }
            }
        }
    }

    private Template edgeX = new Template(new float[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } });

    private Template edgeY = new Template(new float[][] { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } });
}
