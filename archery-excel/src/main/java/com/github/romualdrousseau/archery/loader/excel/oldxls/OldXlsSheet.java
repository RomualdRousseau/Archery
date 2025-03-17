package com.github.romualdrousseau.archery.loader.excel.oldxls;

import java.io.IOException;
import java.util.ArrayList;

import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

import jxl.Sheet;
import jxl.Cell;
import jxl.CellType;
import jxl.Range;

public class OldXlsSheet extends PatcheableSheetStore {

    // private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    public OldXlsSheet(final Sheet sheet) throws IOException {
        this.sheet = sheet;
        this.mergedRegions = new ArrayList<Range>();
        for (final var region : this.sheet.getMergedCells()) {
            this.mergedRegions.add(region);
        }
    }

    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        final var row = this.sheet.getRow(rowIndex);
        if (row == null) {
            return 0;
        }
        return row.length - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.sheet.getRows()) {
            return false;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return true;
        } else {
            final var cells = this.sheet.getRow(n);
            return cells != null && this.hasData(cells[colIndex]);
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.sheet.getRows()) {
            return null;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return patchCell;
        } else {
            final var cells = this.sheet.getRow(n);
            return cells != null ? this.getData(cells[colIndex]) : null;
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return 1;
        }

        int numberOfCells = 0;
        for (final var region : this.mergedRegions) {
            if (this.isInRange(region, rowIndex, colIndex)) {
                numberOfCells = region.getBottomRight().getColumn() - region.getTopLeft().getColumn();
                break;
            }
        }

        return numberOfCells + 1;
    }

    @Override
    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final String value, final boolean unmergeAll) {
        final String newCell;
        if (value == null) {
            newCell = this.getCellDataAt(colIndex1, rowIndex1);
        } else {
            newCell = value;
        }

        if (!unmergeAll) {
            this.unmergeCell(colIndex2, rowIndex2);
        }

        final var n2 = this.getInternalMergeDown(colIndex2, rowIndex2);
        this.addPatchCell(colIndex2, n2, newCell);
    }

    private void unmergeCell(final int colIndex, final int rowIndex) {
        final var regionsToRemove = new ArrayList<Range>();
        for (final var region : this.mergedRegions) {
            if (this.isInRange(region, rowIndex, colIndex)) {
                regionsToRemove.add(region);
            }
        }
        for (final var region : regionsToRemove) {
            this.mergedRegions.remove(region);
        }
    }

    private int getInternalMergeDown(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return rowIndex;
        }

        int rowToReturn = rowIndex;
        for (final var region : this.mergedRegions) {
            if (region.getBottomRight().getRow() > region.getTopLeft().getRow()
                    && rowIndex > region.getTopLeft().getRow()
                    && this.isInRange(region, rowIndex, colIndex)) {
                rowToReturn = region.getTopLeft().getRow();
                break;
            }
        }

        return rowToReturn;
    }

    private boolean hasData(final Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellType type = cell.getType();
        return !type.equals(CellType.EMPTY);
    }

    private String getData(final Cell cell) {
        if (cell == null) {
            return null;
        }

        return StringUtils.cleanToken(cell.getContents());
    }

    private boolean isInRange(final Range range, final int rowIndex, final int colIndex) {
        return range.getTopLeft().getColumn() <= colIndex && colIndex <= range.getBottomRight().getColumn()
                && range.getTopLeft().getRow() <= rowIndex && rowIndex <= range.getBottomRight().getRow();
    }

    private final Sheet sheet;
    private final ArrayList<Range> mergedRegions;
}
