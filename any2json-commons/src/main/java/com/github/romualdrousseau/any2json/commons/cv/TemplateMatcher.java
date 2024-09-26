package com.github.romualdrousseau.any2json.commons.cv;

import java.util.ArrayList;
import java.util.List;

public class TemplateMatcher {

    public TemplateMatcher(Template template) {
        this.template = template;
    }

    public List<SearchPoint> matchAll(ISearchBitmap searchBitmap, int x, int y, int w, int h, double threshold) {
        ArrayList<SearchPoint> result = new ArrayList<SearchPoint>();
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                float sad = this.template.sad(searchBitmap, j, i);
                float score = this.template.normalize(sad);
                if (score > threshold) {
                    result.add(new SearchPoint(j, i, sad));
                }
            }
        }
        return result;
    }

    public SearchPoint matchFirst(ISearchBitmap searchBitmap, int x, int y, int w, int h, double threshold) {
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                float sad = this.template.sad(searchBitmap, j, i);
                float score = this.template.normalize(sad);
                if (score > threshold) {
                    return new SearchPoint(j, i, sad);
                }
            }
        }
        return null;
    }

    public SearchPoint matchBest(ISearchBitmap searchBitmap, int x, int y, int w, int h) {
        SearchPoint result = null;
        double maxScore = 0.0;
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                float sad = this.template.sad(searchBitmap, j, i);
                float score = this.template.normalize(sad);
                if (score > maxScore) {
                    maxScore = score;
                    result = new SearchPoint(j, i, sad);
                }
            }
        }
        return result;
    }

    private Template template;
}
