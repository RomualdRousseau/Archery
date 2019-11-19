package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.ITable;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.SheetBitmap;
import com.github.romualdrousseau.any2json.v2.base.Sheet;
import com.github.romualdrousseau.any2json.v2.base.Table;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.templatematching.TemplateMatcher;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

public abstract class IntelliSheet extends Sheet {

    @Override
    public ITable getTable(ITagClassifier classifier) {
        return super.getTable(classifier);
    }

    public List<ITable> findAllTables(ITagClassifier classifier) {
        ArrayList<ITable> result = new ArrayList<ITable>();

        List<SearchPoint[]> rectangles = findAllRectangles(classifier.getSampleCount(), this.getLastRowNum());

        for (SearchPoint[] table : rectangles) {
            if (table[1].getX() >= table[0].getX()) {
                int lastColumnNum = Math.max(table[1].getX(), this.getLastColumnNum(table[0].getX(), table[0].getY()));
                int lastRowNum = ((table[1].getY() + 1) < classifier.getSampleCount()) ? table[1].getY() : this.getLastRowNum();
                result.add(new Table(this, table[0].getX(), table[0].getY(), lastColumnNum, lastRowNum, 0, classifier));
            }
        }

        return result;
    }

    public List<SearchPoint[]> findAllRectangles(int columns, int rows) {
        ISearchBitmap original = new SheetBitmap(this, columns, rows);
        ISearchBitmap filtered = original.clone();

        final Filter filter = new Filter(new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
        filter.apply(original, filtered, 0.5);
        List<SearchPoint[]> rectangles = new RectangleExtractor().extractAll(filtered);

        for (SearchPoint[] rectangle : rectangles) {
            rectangle[0] = new SearchPoint(Math.max(0, rectangle[0].getX() - 1), rectangle[0].getY(), rectangle[0].getSAD());
        }

        final TemplateMatcher pointTemplate = new TemplateMatcher(new Template(new float[][] { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 0, 0 } }));
        List<SearchPoint> points = pointTemplate.matchAll(filtered, 0, 0, filtered.getWidth(), filtered.getHeight(), 0.9);
        for (SearchPoint point : points) {
            rectangles.add(new SearchPoint[] { point, new SearchPoint(point.getX() + 1, point.getY(), point.getSAD()) });
        }

        rectangles = SearchPoint.TrimInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original);

        return rectangles;
    }
}
