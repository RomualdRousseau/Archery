package com.github.romualdrousseau.any2json.parser.sheet;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.SheetParser;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseTable;
import com.github.romualdrousseau.any2json.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.shapeextractor.RectangleExtractor;

public class SheetBitmapParser implements SheetParser {

    @Override
    public List<BaseTable> findAllTables(final BaseSheet sheet) {
        final SheetBitmap image = this.getSheetBitmap(sheet);
        if (!sheet.notifyStepCompleted(new BitmapGeneratedEvent(sheet, image))) {
            return null;
        }
        return this.findAllTables(sheet, image);
    }

    private SheetBitmap getSheetBitmap(final BaseSheet sheet) {
        return new SheetBitmap(sheet, sheet.getLastColumnNum(), sheet.getLastRowNum());
    }

    private List<BaseTable> findAllTables(final BaseSheet sheet, final SheetBitmap image) {
        final var result = new ArrayList<BaseTable>();

        final var rectangles = this.findAllRectangles(image, sheet.getCapillarityThreshold());
        for (final SearchPoint[] rectangle : rectangles) {
            final var firstColumnNum = rectangle[0].getX();
            final var lastColumnNum = rectangle[1].getX();
            final var lastRowNum = rectangle[1].getY();
            var firstRowNum = rectangle[0].getY();

            if (firstColumnNum > lastColumnNum || firstRowNum > lastRowNum) {
                continue;
            }

            final var table = new BaseTable(sheet, firstColumnNum, firstRowNum, lastColumnNum,
                    lastRowNum);

            boolean isSplitted = false;
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                final var row = table.getRowAt(i);
                if (row.isEmpty()) {
                    final int currRowNum = table.getFirstRow() + i;
                    if (firstRowNum <= (currRowNum - 1)) {
                        result.add(new BaseTable(table, firstRowNum, currRowNum - 1));
                    }
                    result.add(new BaseTable(table, currRowNum, currRowNum));
                    firstRowNum = currRowNum + 1;
                    isSplitted |= true;
                }
            }

            if (!isSplitted) {
                result.add(table);
            } else if (firstRowNum <= lastRowNum) {
                result.add(new BaseTable(table, firstRowNum, lastRowNum));
            }
        }

        return result;
    }

    private List<SearchPoint[]> findAllRectangles(final SheetBitmap original, final float threshold) {
        final var filtered = original.clone();
        if (threshold == 0.5f) {
            final Filter filter = new Filter(new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
            filter.apply(original, filtered, 0.5f);
        } else if (threshold == 1.0f) {
            final Filter filter = new Filter(new Template(new float[][] { { 0, 1, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
            filter.apply(original, filtered, 0.5f);
        } else if (threshold == 1.5f) {
            final Filter filter = new Filter(new Template(new float[][] { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 } }));
            filter.apply(original, filtered, 0.5f);
        }

        var rectangles = new RectangleExtractor().extractAll(filtered);

        for (final var rectangle : rectangles) {
            rectangle[0].setX(Math.max(0, rectangle[0].getX() - 1));
        }

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        final var points = extractAllSearchPoints(original, rectangles);

        for (final var point : points) {
            final var neighboor = new SearchPoint(point.getX() + 1, point.getY(), point.getSAD());
            rectangles.add(new SearchPoint[] { point, neighboor });
        }

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        return rectangles;
    }

    private List<SearchPoint> extractAllSearchPoints(final ISearchBitmap filtered,
            final List<SearchPoint[]> rectangles) {
        final var result = new ArrayList<SearchPoint>();
        for (int i = 0; i < filtered.getHeight(); i++) {
            for (int j = 0; j < filtered.getWidth(); j++) {
                if (filtered.get(j, i) > 0) {
                    boolean isOutside = true;
                    for (final var rectangle : rectangles) {
                        if (SearchPoint.IsInside(rectangle, j, i)) {
                            isOutside = false;
                            break;
                        }
                    }
                    if (isOutside) {
                        result.add(new SearchPoint(j, i, 0));
                    }
                }
            }
        }
        return result;
    }
}
