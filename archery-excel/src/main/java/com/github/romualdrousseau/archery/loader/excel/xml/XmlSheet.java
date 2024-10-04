package com.github.romualdrousseau.archery.loader.excel.xml;

import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

class XmlSheet extends PatcheableSheetStore {

    public XmlSheet(final Worksheet sheet) {
        this.sheet = sheet;
    }

    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        final Row row = this.sheet.getRowAt(rowIndex + 1);
        return row.maxCellIndex() - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows().size() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.sheet.getRows().size()) {
            return false;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return true;
        } else {
            final var cells = this.sheet.getRowAt(n + 1);
            return cells != null && cells.getCellAt(colIndex + 1).hasData();
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.sheet.getRows().size()) {
            return null;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return patchCell;
        } else {
            final var cells = this.sheet.getRowAt(n + 1);
            return cells != null ? StringUtils.cleanToken(cells.getCellAt(colIndex + 1).getData$()) : null;
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        final Cell cell = this.sheet.getCellAt(rowIndex + 1, colIndex + 1);
        return cell.getMergeAcross() + 1;
    }

    @Override
    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2, final String value, final boolean unmergeAll) {
        final String newCell;
        if (value == null) {
            newCell = this.getCellDataAt(colIndex1, rowIndex1);
        } else {
            newCell = value;
        }

        final var n2 = this.getInternalMergeDown(colIndex2, rowIndex2);
        this.addPatchCell(colIndex2, n2, newCell);
    }

    private int getInternalMergeDown(final int colIndex, final int rowIndex) {
        if (rowIndex <= 0) {
            return 0;
        }

        var rowToReturn = rowIndex;
        for (var i = 1; i < 5; i++) {
            final var firstRow = rowIndex - i;
            if (firstRow < 0) {
                break;
            }

            final var lastRow = firstRow + this.sheet.getCellAt(firstRow + 1, colIndex + 1).getMergeDown();
            if (lastRow > firstRow && firstRow <= rowIndex && rowIndex <= lastRow) {
                rowToReturn = firstRow;
                break;
            }
        }

        return rowToReturn;
    }

    private final Worksheet sheet;
}
