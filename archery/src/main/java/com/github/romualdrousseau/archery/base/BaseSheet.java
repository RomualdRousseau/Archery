package com.github.romualdrousseau.archery.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.PivotOption;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;
import com.github.romualdrousseau.archery.SheetListener;
import com.github.romualdrousseau.archery.SheetParser;
import com.github.romualdrousseau.archery.Table;
import com.github.romualdrousseau.archery.TableGraph;
import com.github.romualdrousseau.archery.TableParser;
import com.github.romualdrousseau.archery.config.Settings;
import com.github.romualdrousseau.archery.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.archery.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.archery.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.archery.event.SheetPreparedEvent;
import com.github.romualdrousseau.archery.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.archery.event.TableReadyEvent;
import com.github.romualdrousseau.archery.intelli.IntelliTable;
import com.github.romualdrousseau.archery.parser.sheet.SimpleSheetParser;
import com.github.romualdrousseau.archery.parser.table.SimpleTableParser;
import com.github.romualdrousseau.archery.TransformableSheet;
import com.github.romualdrousseau.archery.commons.collections.CollectionUtils;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class BaseSheet implements Sheet {

    public BaseSheet(final BaseDocument document, final String name, final PatcheableSheetStore store) {
        this.document = document;
        this.name = name;
        this.sheetStore = store;
        this.storeLastColumnNum = this.computeLastColumnNum();
        this.columnMask = CollectionUtils.mutableRange(0, this.storeLastColumnNum + 1);
        this.rowMask = CollectionUtils.mutableRange(0, this.sheetStore.getLastRowNum() + 1);

        this.pivotEnabled = true;
        this.metaEnabled = true;
        this.autoCropEnabled = false;
        this.autoHeaderNameEnabled = true;
        this.autoMetaEnabled = true;

        this.pivotOption = PivotOption.NONE;
        this.pivotKeyFormat = "%s " + Settings.PIVOT_KEY_SUFFIX;
        this.pivotValueFormat = "%s " + Settings.PIVOT_VALUE_SUFFIX;
        this.pivotTypeFormat = "%s " + Settings.PIVOT_TYPE_SUFFIX;
        this.groupValueFormat = "%s " + Settings.GROUP_VALUE_SUFFIX;
        this.columnValueFormat = "%s " + Settings.COLUMN_VALUE_SUFFIX;
    }

    @Override
    public Document getDocument() {
        return this.document;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLastRowNum() {
        return this.rowMask.size() - 1;
    }

    @Override
    public int getLastColumnNum() {
        return this.columnMask.size() - 1;
    }

    @Override
    public void addSheetListener(final SheetListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void applyTransformations() {
        if (this.transfoApplied) {
            return;
        }
        if (this.getLastRowNum() <= 0 || this.getLastColumnNum() <= 0) {
            return;
        }
        TransformableSheet.of(this).applyAll();
        this.transfoApplied = true;
    }

    @Override
    public Optional<TableGraph> getTableGraph() {
        return this.getTableGraph(
                true,
                this.document.getSheetParser(),
                this.document.getTableParser(),
                this.document.getHints());
    }

    @Override
    public Optional<Table> getTable() {
        return this.getTable(true,
                this.document.getSheetParser(),
                this.document.getTableParser(),
                this.document.getHints());
    }

    @Override
    public Optional<TableGraph> getRawTableGraph() {
        return this.getTableGraph(
                false,
                new SimpleSheetParser(),
                new SimpleTableParser(),
                EnumSet.noneOf(Document.Hint.class));
    }

    @Override
    public Optional<Table> getRawTable() {
        return this.getTable(
                false,
                new SimpleSheetParser(),
                new SimpleTableParser(),
                EnumSet.noneOf(Document.Hint.class));
    }

    public SheetStore getSheetStore() {
        return this.sheetStore;
    }

    public int getLastColumnNum(final int rowIndex) {
        final var translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return -1;
        }
        final var lastColumnNum = this.sheetStore.getLastColumnNum(translatedRow);
        return (int) this.columnMask.stream().filter(x -> x <= lastColumnNum).count();
    }

    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return false;
        }
        final var translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return false;
        }
        return this.sheetStore.hasCellDataAt(translatedColumn, translatedRow);
    }

    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return null;
        }
        final var translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return null;
        }
        return this.sheetStore.getCellDataAt(translatedColumn, translatedRow);
    }

    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (this.unmergedAll) {
            return 1;
        }
        final var translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return 1;
        }
        final var translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return 1;
        }
        return this.sheetStore.getNumberOfMergedCellsAt(translatedColumn, translatedRow);
    }

    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final String value) {
        final var translatedColumn1 = this.translateColumn(colIndex1);
        if (translatedColumn1 < 0) {
            return;
        }
        final var translatedRow1 = this.translateRow(rowIndex1);
        if (translatedRow1 < 0) {
            return;
        }
        final var translatedColumn2 = this.translateColumn(colIndex2);
        if (translatedColumn2 < 0) {
            return;
        }
        final var translatedRow2 = this.translateRow(rowIndex2);
        if (translatedRow2 < 0) {
            return;
        }
        this.sheetStore.patchCell(translatedColumn1, translatedRow1, translatedColumn2, translatedRow2, value,
                this.unmergedAll);
    }

    public void patchCells(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final List<String> values) {
        int colIndex = colIndex2;
        for (final var value : values) {
            if (value != null) {
                this.patchCell(colIndex1, rowIndex1, colIndex, rowIndex2, value);
            }
            colIndex++;
        }
    }

    public List<Integer> searchCell(final String regex, final int offset, final int length, final int nth) {
        int n = 0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < this.getLastColumnNum(offset + i); j++) {
                final var cell = this.getCellDataAt(j, offset + i);
                if (!StringUtils.isFastBlank(cell) && cell.matches(regex)) {
                    if (++n == nth) {
                        return List.of(j, offset + i);
                    }
                }
            }
        }
        return null;
    }

    public boolean notifyStepCompleted(final SheetEvent e) {
        for (final var listener : listeners) {
            listener.stepCompleted(e);
        }
        return !e.isCanceled();
    }

    public void markColumnAsNull(final int colIndex) {
        if (colIndex < this.columnMask.size()) {
            this.columnMask.set(colIndex, null);
        }
    }

    public void removeAllNullColumns() {
        this.columnMask.removeIf(i -> i == null);
    }

    public void markRowAsNull(final int rowIndex) {
        if (rowIndex < this.rowMask.size()) {
            this.rowMask.set(rowIndex, null);
        }
    }

    public void removeAllNullRows() {
        this.rowMask.removeIf(i -> i == null);
    }

    public void unmergeAll() {
        this.unmergedAll = true;
    }

    public float getCapillarityThreshold() {
        return this.capillarityThreshold;
    }

    public void setCapillarityThreshold(final float threshold) {
        this.capillarityThreshold = threshold;
    }

    public boolean isMetaEnabled() {
        return this.metaEnabled;
    }

    public void enableMeta() {
        this.metaEnabled = true;
    }

    public void disableMeta() {
        this.metaEnabled = false;
    }

    public boolean isAutoCropEnabled() {
        return this.autoCropEnabled;
    }

    public void enableAutoCrop() {
        this.autoCropEnabled = true;
    }

    public void disableAutoCrop() {
        this.autoCropEnabled = false;
    }

    public boolean isAutoHeaderNameEnabled() {
        return this.autoHeaderNameEnabled;
    }

    public void enableAutoHeaderName() {
        this.autoHeaderNameEnabled = true;
    }

    public void disableAutoHeaderName() {
        this.autoHeaderNameEnabled = false;
    }

    public boolean isAutoMetaEnabled() {
        return this.autoMetaEnabled;
    }

    public void enableAutoMeta() {
        this.autoMetaEnabled = true;
    }

    public void disableAutoMeta() {
        this.autoMetaEnabled = false;
    }

    public boolean isPivotEnabled() {
        return this.pivotEnabled;
    }

    public void enablePivot() {
        this.pivotEnabled = true;
    }

    public void disablePivot() {
        this.pivotEnabled = false;
    }

    public PivotOption getPivotOption() {
        return this.pivotOption;
    }

    public void setPivotOption(final PivotOption option) {
        this.pivotOption = option;
    }

    public List<String> getPivotKeyEntityList() {
        if (!this.pivotEnabled) {
            return Collections.emptyList();
        }
        if (this.pivotKeyEntityList == null) {
            return this.getDocument().getModel().getPivotEntityList();
        }
        return this.pivotKeyEntityList;
    }

    public void setPivotKeyEntityList(final List<String> pivotKeyEntityList) {
        this.pivotKeyEntityList = pivotKeyEntityList;
    }

    public List<String> getPivotTypeEntityList() {
        if (!this.pivotEnabled) {
            return Collections.emptyList();
        }
        if (this.pivotTypeEntityList == null) {
            return Collections.emptyList();
        }
        return this.pivotTypeEntityList;
    }

    public void setPivotTypeEntityList(final List<String> pivotTypeEntityList) {
        this.pivotTypeEntityList = pivotTypeEntityList;
    }

    public String getPivotKeyFormat() {
        return this.pivotKeyFormat;
    }

    public void setPivotKeyFormat(final String format) {
        this.pivotKeyFormat = format;
    }

    public String getPivotTypeFormat() {
        return this.pivotTypeFormat;
    }

    public void setPivotTypeFormat(final String format) {
        this.pivotTypeFormat = format;
    }

    public String getPivotValueFormat() {
        return this.pivotValueFormat;
    }

    public void setPivotValueFormat(final String format) {
        this.pivotValueFormat = format;
    }

    public String getGroupValueFormat() {
        return this.groupValueFormat;
    }

    public void setGroupValueFormat(final String format) {
        this.groupValueFormat = format;
    }

    public String getColumnValueFormat() {
        return this.columnValueFormat;
    }

    public void setColumnValueFormat(final String format) {
        this.columnValueFormat = format;
    }

    public void swapRows(int rowIndex1, int rowIndex2) {
        final var tmp = this.rowMask.get(rowIndex1);
        this.rowMask.set(rowIndex1, this.rowMask.get(rowIndex2));
        this.rowMask.set(rowIndex2, tmp);
    }

    private int translateColumn(final int colIndex) {
        if (colIndex < 0 || colIndex >= this.columnMask.size()) {
            return -1;
        }
        return Optional.of(this.columnMask.get(colIndex)).orElse(-1);
    }

    private int translateRow(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.rowMask.size()) {
            return -1;
        }
        return Optional.of(this.rowMask.get(rowIndex)).orElse(-1);
    }

    private int computeLastColumnNum() {
        if (this.sheetStore.getLastRowNum() < 0) {
            return -1;
        }
        return IntStream.rangeClosed(0, this.sheetStore.getLastRowNum())
                .map(this.sheetStore::getLastColumnNum).max().getAsInt();
    }

    private Optional<TableGraph> getTableGraph(final boolean applyTransformation, final SheetParser sheetParser,
            final TableParser tableParser,
            final EnumSet<Document.Hint> hints) {

        // Here is the core of the algorithm

        if (this.getLastRowNum() <= 0 || this.getLastColumnNum() <= 0) {
            return Optional.empty();
        }

        // Apply transformations

        if (applyTransformation) {
            this.applyTransformations();
        }
        if (!this.notifyStepCompleted(new SheetPreparedEvent(this))) {
            return Optional.empty();
        }

        // Find datatables and metatables

        final var tables = sheetParser.findAllTables(this);
        if (!this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables))) {
            return Optional.empty();
        }

        final var dataTables = tableParser.getDataTables(this, tables);
        if (!this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables))) {
            return Optional.empty();
        }
        if (dataTables.size() == 0) {
            return Optional.empty();
        }

        final List<MetaTable> metaTables;
        if (this.metaEnabled) {
            metaTables = tableParser.getMetaTables(this, tables);
        } else {
            metaTables = Collections.emptyList();
        }
        if (!this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables))) {
            return Optional.empty();
        }

        if (!hints.contains(Document.Hint.INTELLI_LAYOUT)) {
            return Optional.of(new BaseTableGraph(dataTables.get(0)));
        }

        // Build table graph: linked the metatable and datatables depending of the
        // reading directional preferences
        // in perception of visual stimuli depending of the cultures and writing
        // systems.

        final var readingDirection = this.document.getReadingDirection();
        final var root = BaseTableGraphBuilder.build(metaTables, dataTables, readingDirection);

        if (!this.notifyStepCompleted(new TableGraphBuiltEvent(this, root))) {
            return Optional.empty();
        }

        return Optional.of(root);
    }

    private Optional<Table> getTable(final boolean applyTransformation, final SheetParser sheetParser,
            final TableParser tableParser,
            final EnumSet<Document.Hint> hints) {

        final var root = this.getTableGraph(applyTransformation, sheetParser, tableParser, hints);
        if (root.isEmpty()) {
            return Optional.empty();
        }

        final DataTable table;
        if (hints.contains(Document.Hint.INTELLI_LAYOUT)) {
            table = new IntelliTable(this, (BaseTableGraph) root.get(), this.autoHeaderNameEnabled);
        } else {
            table = (DataTable) root.get().getTable();
        }

        // Tag headers

        table.updateHeaderTags();
        this.notifyStepCompleted(new TableReadyEvent(this, table));

        return Optional.of(table);
    }

    private final BaseDocument document;
    private final String name;
    private final PatcheableSheetStore sheetStore;
    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
    private final List<Integer> rowMask;
    private final List<Integer> columnMask;
    private final int storeLastColumnNum;

    private boolean transfoApplied = false;
    private boolean unmergedAll = false;
    private float capillarityThreshold = Settings.DEFAULT_CAPILLARITY_THRESHOLD;
    private boolean pivotEnabled;
    private boolean metaEnabled;
    private boolean autoCropEnabled;
    private boolean autoHeaderNameEnabled;
    private boolean autoMetaEnabled;
    private PivotOption pivotOption;
    private String pivotKeyFormat;
    private String pivotValueFormat;
    private String pivotTypeFormat;
    private String groupValueFormat;
    private String columnValueFormat;
    private List<String> pivotKeyEntityList;
    private List<String> pivotTypeEntityList;
}
