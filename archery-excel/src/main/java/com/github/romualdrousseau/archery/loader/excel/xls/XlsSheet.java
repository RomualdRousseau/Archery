package com.github.romualdrousseau.archery.loader.excel.xls;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class XlsSheet extends PatcheableSheetStore {

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mm:ss");

    private final XlsDocument document;
    private final Sheet sheet;
    private final ArrayList<CellRangeAddress> mergedRegions;

    public XlsSheet(final XlsDocument document, final Sheet sheet) throws IOException {
        this.document = document;
        this.sheet = sheet;
        this.mergedRegions = new ArrayList<CellRangeAddress>();
        for (int j = 0; j < this.sheet.getNumMergedRegions(); j++) {
            final var region = this.sheet.getMergedRegion(j);
            this.mergedRegions.add(region);
        }
    }

    public String getName() {
        return this.sheet.getSheetName();
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        final Row row = this.sheet.getRow(rowIndex);
        if (row == null) {
            return 0;
        }
        return row.getLastCellNum();
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getLastRowNum();
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n > this.sheet.getLastRowNum()) {
            return false;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return true;
        } else {
            final var cells = this.sheet.getRow(n);
            return cells != null && this.hasData(cells.getCell(colIndex));
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n > this.sheet.getLastRowNum()) {
            return null;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return patchCell;
        } else {
            final var cells = this.sheet.getRow(n);
            return cells != null ? this.getData(cells.getCell(colIndex)) : null;
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return 1;
        }

        int numberOfCells = 0;
        for (final var region : this.mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
                numberOfCells = region.getLastColumn() - region.getFirstColumn();
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
        final var regionsToRemove = new ArrayList<CellRangeAddress>();
        for (final var region : this.mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
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
            if (region.getLastRow() > region.getFirstRow() && rowIndex > region.getFirstRow()
                    && region.isInRange(rowIndex, colIndex)) {
                rowToReturn = region.getFirstRow();
                break;
            }
        }

        return rowToReturn;
    }

    private boolean hasData(final Cell cell) {
        if (cell == null) {
            return false;
        }

        final var type = cell.getCellType();
        return !type.equals(CellType.BLANK);
    }

    private String getData(final Cell cell) {
        if (cell == null) {
            return null;
        }

        var type = cell.getCellType();
        if (type.equals(CellType.FORMULA)) {
            type = cell.getCachedFormulaResultType();
        }

        String value = "";

        switch (type) {
            case STRING:
                value = StringUtils.cleanToken(cell.getRichStringCellValue().getString());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (this.document.getHints().contains(Document.Hint.INTELLI_TIME)) {
                        final var dataFormatString = cell.getCellStyle().getDataFormatString();
                        if (dataFormatString.contains("h") && !dataFormatString.contains("d")
                                && !dataFormatString.contains("y")) {
                            value = TIME_FORMATTER.format(cell.getDateCellValue());
                        } else {
                            value = DATETIME_FORMATTER.format(cell.getDateCellValue());
                        }
                    } else {
                        value = DATE_FORMATTER.format(cell.getDateCellValue());
                    }
                } else {
                    final double d = cell.getNumericCellValue();
                    if (d != Math.rint(d)) {
                        value = String.valueOf(cell.getNumericCellValue());
                    } else {
                        value = String.valueOf((int) cell.getNumericCellValue());
                    }
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue() ? "TRUE" : "FALSE";
                break;
            default:
                // Do nothing
        }

        return value;
    }
}
