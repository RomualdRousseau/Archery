package com.github.romualdrousseau.archery.commons.cv.shapeextractor;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.archery.commons.cv.ISearchBitmap;
import com.github.romualdrousseau.archery.commons.cv.IShapeExtractor;
import com.github.romualdrousseau.archery.commons.cv.SearchPoint;
import com.github.romualdrousseau.archery.commons.cv.Template;
import com.github.romualdrousseau.archery.commons.cv.TemplateMatcher;

public class RectangleExtractor extends IShapeExtractor {

    @Override
    public List<SearchPoint[]> extractAll(ISearchBitmap searchBitmap) {
        ArrayList<SearchPoint[]> result = new ArrayList<SearchPoint[]>();

        ArrayList<List<SearchPoint>> allCorners = new ArrayList<List<SearchPoint>>();
        allCorners.add(
                cornerTopLeft.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerTopRight.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerBottomRight.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerBottomLeft.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));

        // Simple version of Hought transformation with 4 pre-defined rotations
        for (int phi = 0; phi < allCorners.size(); phi++) {
            for (SearchPoint corner : allCorners.get(phi)) {
                SearchPoint[] a = houghTransform(phi, corner, allCorners);
                if (count(a) < (a.length - 1)) {
                    continue;
                }

                SearchPoint[] bbox = minmax(phi, a);

                if (searchBitmap.get(bbox[0].getX(), bbox[0].getY()) > 0
                        && searchBitmap.get(bbox[1].getX(), bbox[0].getY()) > 0
                        && searchBitmap.get(bbox[1].getX(), bbox[1].getY()) > 0
                        && searchBitmap.get(bbox[0].getX(), bbox[1].getY()) > 0) {
                    if(SearchPoint.isValid(bbox) && !SearchPoint.IsDuplicate(bbox, result)) {
                        result.add(bbox);
                    }
                }
            }
        }
        if (result.size() > 1) {
            return SearchPoint.RemoveOverlaps(result);
        } else {
            return result;
        }
    }

    @Override
    public SearchPoint[] extractBest(ISearchBitmap searchBitmap) {
        SearchPoint[] result = null;
        int maxArea = 0;

        ArrayList<List<SearchPoint>> allCorners = new ArrayList<List<SearchPoint>>();
        allCorners.add(
                cornerTopLeft.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerTopRight.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerBottomRight.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));
        allCorners.add(
                cornerBottomLeft.matchAll(searchBitmap, 0, 0, searchBitmap.getWidth(), searchBitmap.getHeight(), 0.8));

        // Simple version of Hought transformation with 4 pre-defined rotations
        for (int phi = 0; phi < allCorners.size(); phi++) {
            for (SearchPoint corner : allCorners.get(phi)) {
                SearchPoint[] a = houghTransform(phi, corner, allCorners);
                if (count(a) < (a.length - 1)) {
                    continue;
                }

                SearchPoint[] bbox = minmax(phi, a);

                if (searchBitmap.get(bbox[0].getX(), bbox[0].getY()) > 0
                        && searchBitmap.get(bbox[1].getX(), bbox[0].getY()) > 0
                        && searchBitmap.get(bbox[1].getX(), bbox[1].getY()) > 0
                        && searchBitmap.get(bbox[0].getX(), bbox[1].getY()) > 0) {
                    int area = SearchPoint.GetArea(bbox);
                    if (area > maxArea) {
                        maxArea = area;
                        result = bbox;
                    }
                }
            }
        }
        return result;
    }

    private SearchPoint[] houghTransform(int phi, SearchPoint locus, List<List<SearchPoint>> points) {
        SearchPoint[] a = { null, null, null, null };

        a[phi] = locus;

        for (int j = 0; j < points.size(); j++) {
            if (j != phi) {
                for (SearchPoint point : points.get(j)) {
                    int[] g = gradient(locus, point);
                    if (g[0] == R[phi][j][0] && g[1] == R[phi][j][1]) {
                        if (a[j] == null || distance(locus, point) < distance(locus, a[j])) {
                            a[j] = point;
                        }
                    }
                }
            }
        }

        return a;
    }

    private int[] gradient(SearchPoint p1, SearchPoint p2) {
        int vx = p2.getX() - p1.getX();
        vx = (vx == 0) ? 0 : ((vx > 0) ? 1 : -1);
        int vy = p2.getY() - p1.getY();
        vy = (vy == 0) ? 0 : ((vy > 0) ? 1 : -1);
        return new int[] { vx, vy };
    }

    private double distance(SearchPoint p1, SearchPoint p2) {
        double vx = p1.getX() - p2.getX();
        double vy = p1.getY() - p2.getY();
        return Math.sqrt(vx * vx + vy * vy);
    }

    private int count(SearchPoint[] points) {
        int count = 0;
        for (int k = 0; k < 4; k++) {
            if (points[k] != null) {
                count++;
            }
        }
        return count;
    }

    private SearchPoint[] minmax(int phi, SearchPoint[] points) {
        int minX = points[phi].getX();
        int minY = points[phi].getY();
        int maxX = points[phi].getX();
        int maxY = points[phi].getY();
        for (int k = 0; k < 4; k++) {
            if (k != phi && points[k] != null) {
                minX = Math.min(minX, points[k].getX());
                minY = Math.min(minY, points[k].getY());
                maxX = Math.max(maxX, points[k].getX());
                maxY = Math.max(maxY, points[k].getY());
            }
        }
        return new SearchPoint[] { new SearchPoint(minX, minY), new SearchPoint(maxX, maxY) };
    }

    private int R[][][] = { { { 0, 0 }, { 1, 0 }, { 2, 2 }, { 0, 1 } }, { { -1, 0 }, { 0, 0 }, { 0, 1 }, { -2, 2 } },
            { { -2, -2 }, { 0, -1 }, { 0, 0 }, { -1, 0 } }, { { 0, -1 }, { 2, -2 }, { 1, 0 }, { 0, 0 } } };

    private TemplateMatcher cornerTopLeft = new TemplateMatcher(
            new Template(new float[][] { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 1, 1 } }));

    private TemplateMatcher cornerTopRight = new TemplateMatcher(
            new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 1, 1, 0 } }));

    private TemplateMatcher cornerBottomLeft = new TemplateMatcher(
            new Template(new float[][] { { 0, 1, 1 }, { 0, 1, 1 }, { 0, 0, 0 } }));

    private TemplateMatcher cornerBottomRight = new TemplateMatcher(
            new Template(new float[][] { { 1, 1, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
}
