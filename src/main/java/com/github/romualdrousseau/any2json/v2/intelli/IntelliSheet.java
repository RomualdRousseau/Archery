package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.Table;
import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractSheet;
import com.github.romualdrousseau.any2json.v2.base.SheetBitmap;
import com.github.romualdrousseau.any2json.v2.intelli.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.IntelliTableReadyEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;
import com.github.romualdrousseau.any2json.v2.util.Visitable;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.templatematching.TemplateMatcher;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

public abstract class IntelliSheet extends AbstractSheet {

    @Override
    public Table getTable(final ITagClassifier classifier) {
        final SheetBitmap image = new SheetBitmap(this, classifier.getSampleCount(), this.getLastRowNum());
        this.notifyStepCompleted(new BitmapGeneratedEvent(this, image));

        final List<CompositeTable> tables = this.findAllTables(classifier, image);
        this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables));

        final List<DataTable> dataTables = this.getDataTables(tables, classifier.getDataLayexes());
        this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables));
        if(dataTables.size() == 0) {
            return null;
        }

        final List<MetaTable> metaTables = this.getMetaTables(tables, classifier.getMetaLayexes());
        this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables));

        final TableGraph root = this.buildTableGraph(metaTables, dataTables);
        this.notifyStepCompleted(new TableGraphBuiltEvent(this, root));

        Table table = new IntelliTable(root, classifier);
        this.notifyStepCompleted(new IntelliTableReadyEvent(this, table));

        return table;
    }

    private TableGraph buildTableGraph(final List<MetaTable> metaTables, final List<DataTable> dataTables) {
        final TableGraph root = new TableGraph();

        for (final Visitable e : metaTables) {
            e.setVisited(false);
        }

        // First attach all not snapped metaTables to the root nodes
        for (final MetaTable metaTable : metaTables) {
            if (!isSnapped(metaTable, dataTables)) {
                root.addChild(new TableGraph(metaTable));
                metaTable.setVisited(true);
            }
        }

        // Second attach all snapped metaTables to the closest nodes
        for (final MetaTable metaTable : metaTables) {
            if (metaTable.isVisited()) {
                continue;
            }

            final TableGraph parent = findClosestMetaGraph(root, metaTable, 0, 0);
            parent.addChild(new TableGraph(metaTable));
            metaTable.setVisited(true);
        }

        // Third attach datatables to the closest metadatas
        for (final DataTable dataTable : dataTables) {
            final TableGraph parent = findClosestMetaGraph(root, dataTable, 0, 1);
            parent.addChild(new TableGraph(dataTable));
        }

        return root;
    }

    private List<DataTable> getDataTables(final List<CompositeTable> tables, final List<LayexMatcher> dataLayexes) {
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        for (final Visitable e : tables) {
            e.setVisited(false);
        }

        for (final CompositeTable table : tables) {
            boolean foundMatch = false;
            for (final LayexMatcher dataLayex : dataLayexes) {
                if (!foundMatch && dataLayex.match(new TableLexer(table), null)) {
                    result.add(new DataTable(table, dataLayex));
                    table.setVisited(true);
                    foundMatch = true;
                }
            }
        }

        return result;
    }

    private List<MetaTable> getMetaTables(final List<CompositeTable> tables, final List<LayexMatcher> metaLayexes) {
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();

        for (final CompositeTable table : tables) {
            if (table.isVisited()) {
                continue;
            }

            boolean foundMatch = false;
            for (final LayexMatcher metaLayex : metaLayexes) {
                if (!foundMatch && metaLayex.match(new TableLexer(table), null)) {
                    result.add(new MetaTable(table, metaLayex));
                    foundMatch = true;
                }
            }
            if (!foundMatch) {
                result.add(new MetaTable(table));
            }

            table.setVisited(true);
        }

        return result;
    }

    private List<CompositeTable> findAllTables(final ITagClassifier classifier, final SheetBitmap image) {
        final ArrayList<CompositeTable> result = new ArrayList<CompositeTable>();

        final List<SearchPoint[]> rectangles = findAllRectangles(image);
        for (final SearchPoint[] rectangle : rectangles) {
            final int firstColumnNum = rectangle[0].getX();
            int firstRowNum = rectangle[0].getY();
            final int lastColumnNum = rectangle[1].getX();
            final int lastRowNum = rectangle[1].getY();

            if(firstColumnNum > lastColumnNum || firstRowNum > lastRowNum) {
                continue;
            }

            final CompositeTable table = new CompositeTable(this, firstColumnNum, firstRowNum, lastColumnNum, lastRowNum,
                    classifier);

            boolean isSplitted = false;
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                final BaseRow row = table.getRowAt(i);
                if (row.sparsity() >= DocumentFactory.DEFAULT_RATIO_SCARSITY
                        && row.density() > DocumentFactory.DEFAULT_RATIO_DENSITY) {
                    final int currRowNum = table.getFirstRow() + i;
                    if (firstRowNum <= (currRowNum - 1)) {
                        result.add(new CompositeTable(table, firstRowNum, currRowNum - 1));
                    }
                    result.add(new CompositeTable(table, currRowNum, currRowNum));
                    firstRowNum = currRowNum + 1;
                    isSplitted |= true;
                }
            }

            if (!isSplitted) {
                result.add(table);
            } else if (firstRowNum <= lastRowNum) {
                result.add(new CompositeTable(table, firstRowNum, lastRowNum));
            }
        }

        return result;
    }

    private List<SearchPoint[]> findAllRectangles(final SheetBitmap original) {
        final ISearchBitmap filtered = original.clone();
        final Filter filter = new Filter(new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
        filter.apply(original, filtered, 0.5);

        List<SearchPoint[]> rectangles = new RectangleExtractor().extractAll(filtered);

        for (final SearchPoint[] rectangle : rectangles) {
            rectangle[0].setX(Math.max(0, rectangle[0].getX() - 1));
        }

        final List<SearchPoint> points = extractAllPoints(filtered);

        for (final SearchPoint point : points) {
            final SearchPoint neighboor = new SearchPoint(point.getX() + 1, point.getY(), point.getSAD());
            rectangles.add(new SearchPoint[] { point, neighboor });
        }

        rectangles = SearchPoint.ExpandInX(SearchPoint.TrimInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original), original);

        return rectangles;
    }

    private List<SearchPoint> extractAllPoints(final ISearchBitmap filtered) {
        final TemplateMatcher pointTemplate = new TemplateMatcher(
                new Template(new float[][] { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 0, 0 } }));
        return pointTemplate.matchAll(filtered, 0, 0, filtered.getWidth(), filtered.getHeight(), 0.9);
    }

    private boolean isSnapped(final MetaTable metaTable, final List<DataTable> dataTables) {
        for (final DataTable dataTable : dataTables) {
            if (distanceBetweenTables(metaTable, dataTable) == 0) {
                return true;
            }
        }
        return false;
    }

    private TableGraph findClosestMetaGraph(final TableGraph root, final CompositeTable table, final int level,
            final int maxLevel) {
        TableGraph result = root;

        if (level > maxLevel) {
            return result;
        }

        double minDist = Double.MAX_VALUE;
        for (final TableGraph child : root.children()) {
            if (!(child.getTable() instanceof MetaTable)) {
                continue;
            }

            final TableGraph grandChild = findClosestMetaGraph(child, table, level + 1, maxLevel);
            final double dist1 = distanceBetweenTables(grandChild.getTable(), table);
            final double dist2 = distanceBetweenTables(child.getTable(), table);

            if (dist1 < dist2) {
                if (dist1 < minDist) {
                    minDist = dist1;
                    result = grandChild;
                }
            } else {
                if (dist2 < minDist) {
                    minDist = dist2;
                    result = child;
                }
            }
        }

        return result;
    }

    private double distanceBetweenTables(final CompositeTable table1, final CompositeTable table2) {
        final int vx = table2.getFirstColumn() - table1.getFirstColumn();
        final int vy = table2.getFirstRow() - table1.getLastRow() - 1;
        if (vx >= 0 && vy >= 0) {
            return vx + vy;
        } else {
            return Double.MAX_VALUE;
        }
    }
}
