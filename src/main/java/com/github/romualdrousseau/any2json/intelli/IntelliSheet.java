package com.github.romualdrousseau.any2json.intelli;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.python.util.PythonInterpreter;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.any2json.base.SheetBitmap;
import com.github.romualdrousseau.any2json.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.any2json.event.SheetPreparedEvent;
import com.github.romualdrousseau.any2json.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.any2json.util.SheetStore;
import com.github.romualdrousseau.any2json.util.TableGraph;
import com.github.romualdrousseau.any2json.util.Visitable;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

public class IntelliSheet extends EditableSheet {

    public IntelliSheet(SheetStore store, boolean documentIsStructured) {
        super(store);
        this.documentIsStructured = documentIsStructured;
    }

    @Override
    public Table createIntelliTable() {
        final List<DataTable> dataTables;
        final List<MetaTable> metaTables;
        if (this.documentIsStructured) {
            final List<CompositeTable> tables = new LinkedList<CompositeTable>();
            tables.add(new CompositeTable(this, 0, 0, this.getLastColumnNum(), this.getLastRowNum()));
            if (!this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables))) {
                return null;
            }

            dataTables = new LinkedList<DataTable>();
            dataTables.add(new DataTable(tables.get(0)));
            if (!this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables))) {
                return null;
            }

            metaTables = new ArrayList<MetaTable>();
            if (!this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables))) {
                return null;
            }
        }
        else {
            this.stichRows();

            final String recipe = this.getClassifierFactory().getLayoutClassifier().get().getRecipe();
            if (recipe != null) {
                try(PythonInterpreter pyInterp = new PythonInterpreter()) {
                    pyInterp.set("sheet", this);
                    pyInterp.exec(recipe);
                }
            }
            if (!this.notifyStepCompleted(new SheetPreparedEvent(this))) {
                return null;
            }

            final SheetBitmap image = this.getSheetBitmap();
            if (!this.notifyStepCompleted(new BitmapGeneratedEvent(this, image))) {
                return null;
            }

            final List<CompositeTable> tables = this.findAllTables(image);
            if (!this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables))) {
                return null;
            }

            dataTables = this.getDataTables(tables,
                    this.getClassifierFactory().getLayoutClassifier().get().getDataMatcherList());
            if (!this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables))) {
                return null;
            }
            if (dataTables.size() == 0) {
                return null;
            }

            metaTables = this.getMetaTables(tables,
                    this.getClassifierFactory().getLayoutClassifier().get().getMetaMatcherList());
            if (!this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables))) {
                return null;
            }
        }

        final TableGraph root = this.buildTableGraph(metaTables, dataTables);
        if (!this.notifyStepCompleted(new TableGraphBuiltEvent(this, root))) {
            return null;
        }

        return new IntelliTable(this, root);
    }

    protected SheetBitmap getSheetBitmap() {
        return new SheetBitmap(this,
                Math.min(this.getLastColumnNum(),
                        this.getClassifierFactory().getLayoutClassifier().get().getSampleCount()),
                this.getLastRowNum());
    }

    protected List<CompositeTable> findAllTables(final SheetBitmap image) {
        final ArrayList<CompositeTable> result = new ArrayList<CompositeTable>();

        final List<SearchPoint[]> rectangles = findAllRectangles(image);
        for (final SearchPoint[] rectangle : rectangles) {
            final int firstColumnNum = rectangle[0].getX();
            int firstRowNum = rectangle[0].getY();
            final int lastColumnNum = rectangle[1].getX();
            final int lastRowNum = rectangle[1].getY();

            if (firstColumnNum > lastColumnNum || firstRowNum > lastRowNum) {
                continue;
            }

            final CompositeTable table = new CompositeTable(this, firstColumnNum, firstRowNum, lastColumnNum,
                    lastRowNum);

            boolean isSplitted = false;
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                final BaseRow row = table.getRowAt(i);
                if (row.isEmpty()) {
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

    protected List<MetaTable> getMetaTables(final List<CompositeTable> tables, final List<TableMatcher> metaMatchers) {
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();

        for (final CompositeTable table : tables) {
            if (table.isVisited()) {
                continue;
            }

            boolean foundMatch = false;
            for (final TableMatcher matcher : metaMatchers) {
                if (!foundMatch && matcher.match(new TableLexer(table, 0), null)) {
                    result.add(new MetaTable(table, matcher));
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

    protected List<DataTable> getDataTables(final List<CompositeTable> tables, final List<TableMatcher> dataMatchers) {
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        for (final Visitable e : tables) {
            e.setVisited(false);
        }

        for (final CompositeTable table : tables) {
            boolean foundMatch = false;
            for (int tryCount = 0; tryCount < 3; tryCount++) {
                if (!foundMatch) {
                    for (final TableMatcher matcher : dataMatchers) {
                        if (!foundMatch && matcher.match(new TableLexer(table, tryCount), null)) {
                            final DataTable dataTable = new DataTable(table, matcher, tryCount);
                            result.add(dataTable);
                            if (dataTable.getContext().getSplitRows().size() > 0) {
                                this.splitAllSubTables(table, matcher, dataTable.getContext(), result);
                            }
                            table.setVisited(true);
                            foundMatch = true;
                        }
                    }
                }
            }
        }

        return result;
    }

    private TableGraph buildTableGraph(final List<MetaTable> metaTables, final List<DataTable> dataTables) {
        final TableGraph root = new TableGraph();

        for (final Visitable e : metaTables) {
            e.setVisited(false);
        }

        // First attach all not snapped metaTables to the root nodes
        for (final MetaTable metaTable : metaTables) {
            if (!isJoint(metaTable, dataTables)) {
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

    private void splitAllSubTables(final CompositeTable table, final TableMatcher layex, final DataTableGroupSubHeaderParser context,
            final List<DataTable> result) {
        int firstRow = -1;
        for (final int splitRow : context.getSplitRows()) {
            if (firstRow >= 0) {
                final CompositeTable subTable = new CompositeTable(table, firstRow, table.getFirstRow() + splitRow - 1);
                result.add(new DataTable(subTable, layex, 0));
            }
            firstRow = table.getFirstRow() + splitRow;
        }
    }

    private List<SearchPoint[]> findAllRectangles(final SheetBitmap original) {
        final ISearchBitmap filtered = original.clone();
        final Filter filter = new Filter(new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
        filter.apply(original, filtered, 0.5);

        List<SearchPoint[]> rectangles = new RectangleExtractor().extractAll(filtered);

        for (final SearchPoint[] rectangle : rectangles) {
            rectangle[0].setX(Math.max(0, rectangle[0].getX() - 1));
        }

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        final List<SearchPoint> points = extractAllSearchPoints(original, rectangles);

        for (final SearchPoint point : points) {
            final SearchPoint neighboor = new SearchPoint(point.getX() + 1, point.getY(), point.getSAD());
            rectangles.add(new SearchPoint[] { point, neighboor });
        }

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        return rectangles;
    }

    private List<SearchPoint> extractAllSearchPoints(final ISearchBitmap filtered, final List<SearchPoint[]> rectangles) {
        final ArrayList<SearchPoint> result = new ArrayList<SearchPoint>();
        for (int i = 0; i < filtered.getHeight(); i++) {
            for (int j = 0; j < filtered.getWidth(); j++) {
                if (filtered.get(j, i) > 0) {
                    boolean isOutside = true;
                    for (final SearchPoint[] rectangle : rectangles) {
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

    private boolean isJoint(final MetaTable metaTable, final List<DataTable> dataTables) {
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

    private final boolean documentIsStructured;
}
