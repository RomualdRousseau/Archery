package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.Table;
import com.github.romualdrousseau.any2json.v2.base.AbstractSheet;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.base.SheetBitmap;
import com.github.romualdrousseau.any2json.v2.intelli.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.IntelliTableReadyEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.any2json.v2.intelli.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;
import com.github.romualdrousseau.any2json.v2.util.Visitable;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.FuzzyString;

public abstract class IntelliSheet extends AbstractSheet implements RowTranslatable {

    public IntelliSheet() {
        this.rowTranslator = new RowTranslator(this);
    }

    @Override
    public Table getTable(final ITagClassifier classifier) {
        if (this.getLastRowNum() < 0) {
            return null;
        }

        this.classifier = classifier;

        final SheetBitmap image = new SheetBitmap(this, classifier.getSampleCount(), this.getLastRowNum() + 1);
        if (!this.notifyStepCompleted(new BitmapGeneratedEvent(this, image))) {
            return null;
        }

        final List<CompositeTable> tables = this.findAllTables(classifier, image);
        if (!this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables))) {
            return null;
        }

        final List<DataTable> dataTables = this.getDataTables(tables, classifier.getDataLayexes());
        if (!this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables))) {
            return null;
        }

        if (dataTables.size() == 0) {
            return null;
        }

        final List<MetaTable> metaTables = this.getMetaTables(tables, classifier.getMetaLayexes());
        if (!this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables))) {
            return null;
        }

        final TableGraph root = this.buildTableGraph(metaTables, dataTables);
        if (!this.notifyStepCompleted(new TableGraphBuiltEvent(this, root))) {
            return null;
        }

        Table table = new IntelliTable(root, classifier);
        this.notifyStepCompleted(new IntelliTableReadyEvent(this, table));

        return table;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        final int translatedRow = this.rowTranslator.translate(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getInternalLastRowNum()) {
            return -1;
        }
        return this.getInternalLastColumnNum(translatedRow);
    }

    @Override
    public int getLastRowNum() {
        return this.getInternalLastRowNum() - this.rowTranslator.getTranslatedRowCount();
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowTranslator.translate(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getInternalLastRowNum()) {
            return false;
        }
        if (colIndex < 0 || colIndex > this.getInternalLastColumnNum(translatedRow)) {
            return false;
        }
        return this.hasInternalCellDataAt(colIndex, translatedRow);
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowTranslator.translate(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getInternalLastRowNum()) {
            return null;
        }
        if (colIndex < 0 || colIndex > this.getInternalLastColumnNum(translatedRow)) {
            return null;
        }
        return this.getInternalCellDataAt(colIndex, translatedRow);
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowTranslator.translate(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getInternalLastRowNum()) {
            return 1;
        }
        if (colIndex < 0 || colIndex > this.getInternalLastColumnNum(translatedRow)) {
            return 1;
        }
        return this.getInternalMergeAcross(colIndex, translatedRow);
    }

    @Override
    public boolean isInvalidRow(int rowIndex) {
        if (rowIndex <= 0 || rowIndex >= this.getInternalLastRowNum()) {
            return false;
        }

        String hash = this.getRowPattern(rowIndex);

        // Remove unwanted row
        if (hash.equals("X")) {
            return true;
        }

        // Keep non empty rows
        if (!hash.isEmpty()) {
            return false;
        }

        // Test if the previous and next rows can be "stiched"
        String hashPrev = this.getRowPattern(rowIndex - 1);
        String hashNext = this.getRowPattern(rowIndex + 1);
        return FuzzyString.Hamming(hashPrev, hashNext) >= DocumentFactory.DEFAULT_RATIO_SIMILARITY;
    }

    protected abstract int getInternalLastColumnNum(int rowIndex);

    protected abstract int getInternalLastRowNum();

    protected abstract boolean hasInternalCellDataAt(int colIndex, int rowIndex);

    protected abstract String getInternalCellDataAt(int colIndex, int rowIndex);

    protected abstract int getInternalMergeAcross(int colIndex, int rowIndex);

    protected abstract int getInternalMergeDown(int colIndex, int rowIndex);

    private String getRowPattern(int rowIndex) {
        String hash = "";
        int countEmptyCells = 0;
        boolean checkIfRowMergedVertically = false;

        for (int i = 0; i < this.getInternalLastColumnNum(rowIndex);) {
            final String value = this.getInternalCellDataAt(i, rowIndex);

            if (value != null) {
                if (value.isEmpty()) {
                    hash += "s";
                    countEmptyCells++;
                } else if (this.classifier != null) {
                    Vector v = this.classifier.getEntityList().word2vec(value);
                    if (v.sparsity() < 1.0f) {
                        hash += "e";
                    } else {
                        hash += "v";
                    }
                } else {
                    hash += "v";
                }
            }

            if (!checkIfRowMergedVertically && this.getInternalMergeDown(i, rowIndex) > 0) {
                checkIfRowMergedVertically = true;
            }

            i += this.getInternalMergeAcross(i, rowIndex);
        }

        if (checkIfRowMergedVertically) {
            hash = "X";
        } else if (countEmptyCells == hash.length()) {
            hash = "";
        }

        return hash;
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

    private List<DataTable> getDataTables(final List<CompositeTable> tables, final List<LayexMatcher> dataLayexes) {
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        for (final Visitable e : tables) {
            e.setVisited(false);
        }

        for (final CompositeTable table : tables) {
            boolean foundMatch = false;
            for (final LayexMatcher dataLayex : dataLayexes) {
                if (!foundMatch && dataLayex.match(new TableLexer(table), null)) {
                    DataTable dataTable = new DataTable(table, dataLayex);
                    result.add(dataTable);

                    if (dataTable.getContext().getSplitRows().size() > 0) {
                        this.splitAllSubTables(table, dataLayex, dataTable.getContext(), result);
                    }

                    table.setVisited(true);
                    foundMatch = true;
                }
            }
        }

        return result;
    }

    private void splitAllSubTables(CompositeTable table, LayexMatcher layex, DataTableContext context,
            List<DataTable> result) {
        int firstRow = -1;
        for (int splitRow : context.getSplitRows()) {
            if (firstRow >= 0) {
                CompositeTable subTable = new CompositeTable(table, firstRow, table.getFirstRow() + splitRow - 1);
                result.add(new DataTable(subTable, layex));
            }
            firstRow = table.getFirstRow() + splitRow;
        }
    }

    private List<CompositeTable> findAllTables(final ITagClassifier classifier, final SheetBitmap image) {
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
                    lastRowNum, classifier);

            boolean isSplitted = false;
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                final BaseRow row = table.getRowAt(i);
                if (row.isEmpty()) {
                    // if (row.density() >= DocumentFactory.DEFAULT_RATIO_DENSITY) {
                    // if (row.sparsity() > DocumentFactory.DEFAULT_RATIO_SCARSITY
                    // && row.density() > DocumentFactory.DEFAULT_RATIO_DENSITY) {
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

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        final List<SearchPoint> points = extractAllPoints(original, rectangles);

        for (final SearchPoint point : points) {
            final SearchPoint neighboor = new SearchPoint(point.getX() + 1, point.getY(), point.getSAD());
            rectangles.add(new SearchPoint[] { point, neighboor });
        }

        rectangles = SearchPoint.TrimInX(
                SearchPoint.ExpandInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original),
                original);

        return rectangles;
    }

    private List<SearchPoint> extractAllPoints(final ISearchBitmap filtered, List<SearchPoint[]> rectangles) {
        ArrayList<SearchPoint> result = new ArrayList<SearchPoint>();

        for (int i = 0; i < filtered.getHeight(); i++) {
            for (int j = 0; j < filtered.getWidth(); j++) {
                if (filtered.get(j, i) > 0) {
                    boolean isOutside = true;
                    for (SearchPoint[] rectangle : rectangles) {
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

    private RowTranslator rowTranslator;
    private ITagClassifier classifier;
}
