package com.github.romualdrousseau.any2json.v2.loader.xls;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;

public class ExcelMappedSheet implements RowTranslatable {

    public ExcelMappedSheet() {
        this.rowTranslator = new RowTranslator(this);
    }

    public File map(Sheet sheet) throws IOException {
        File result = File.createTempFile("any2json", ".tmp");
        result.deleteOnExit();

        this.sheet = sheet;

        this.cachedRegion = new ArrayList<CellRangeAddress>();
        for (int j = 0; j < this.sheet.getNumMergedRegions(); j++) {
            CellRangeAddress region = this.sheet.getMergedRegion(j);
            this.cachedRegion.add(region);
        }

        try (ObjectOutputStream buffer = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(result)))) {
            for (int i = 0; i <= this.sheet.getLastRowNum(); i++) {
                Row orgRow = this.getRowAt(i);
                if (orgRow == null) {
                    continue;
                }

                MappedRow row = new MappedRow();
                MappedCell lastCell = null;

                for (int j = 0; j <= orgRow.getLastCellNum();) {
                    Cell orgCell = orgRow.getCell(j);
                    String value = (this.hasData(orgCell)) ? this.getValue(orgCell) : null;
                    int length = this.getMergeAcross(orgCell);

                    if (lastCell == null) {
                        lastCell = new MappedCell();
                        lastCell.value = value;
                        lastCell.length = length;
                    } else if (value == null && lastCell.value == null
                            || value != null && lastCell.value != null && !lastCell.value.equals(value)) {
                        row.cells.add(lastCell);
                        lastCell = new MappedCell();
                        lastCell.value = value;
                        lastCell.length = length;
                    } else {
                        lastCell.length++;
                    }

                    j += length;
                }

                if (lastCell != null) {
                    row.cells.add(lastCell);
                }

                row.lastColumnNum = orgRow.getLastCellNum();
                buffer.writeObject(row);
            }

            buffer.writeObject(null);
        }

        this.sheet = null;
        this.cachedRegion = null;
        Runtime.getRuntime().gc();

        return result;
    }

    public List<MappedRow> unmap(File file) {
        try (ObjectInputStream buffer = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ArrayList<MappedRow> rows = new ArrayList<MappedRow>();
            Object row;
            while ((row = buffer.readObject()) != null) {
                rows.add((MappedRow) row);
            }
            return rows;
        } catch (IOException | ClassNotFoundException ignore) {
            return null;
        }
    }

    @Override
    public boolean isIgnorableRow(int rowIndex) {
        if (rowIndex > (this.sheet.getLastRowNum() + 1)) {
            return false;
        }

        Row row = this.sheet.getRow(rowIndex);
        if (row == null || rowIndex > this.sheet.getLastRowNum()) {
            return false;
        }

        double height = row.getHeight() * 0.07; // Rougly convert in pixels
        int merged = this.getMergeDown(row);
        float sparcity = Float.valueOf(merged) / Float.valueOf(row.getLastCellNum() - row.getFirstCellNum());

        boolean candidate = false;
        candidate |= (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
        candidate |= (merged > 0);
        candidate &= (sparcity >= DocumentFactory.DEFAULT_RATIO_SCARSITY);
        return candidate;
    }

    private Row getRowAt(int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(rowIndex);
        if (translatedRow == -1) {
            return null;
        }
        return this.sheet.getRow(translatedRow);
    }

    private boolean hasData(Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellType type = cell.getCellType();

        if (type.equals(CellType.BLANK) || type.equals(CellType.STRING) && cell.getStringCellValue().isEmpty()) {
            if (cell.getRowIndex() == 0) {
                return false;
            }

            final CellStyle style = cell.getCellStyle();

            // Keep cell with colored borders
            if (!style.getBorderLeft().equals(BorderStyle.NONE) && !style.getBorderRight().equals(BorderStyle.NONE)
                    && !style.getBorderTop().equals(BorderStyle.NONE)
                    && !style.getBorderBottom().equals(BorderStyle.NONE)) {
                // if (style.getLeftBorderColor() != 0 && style.getRightBorderColor() != 0
                // && style.getTopBorderColor() != 0 && style.getBottomBorderColor() != 0) {
                // return true;
                // }
                return true;
            }

            // Keep cell with a colored (not automatic and not white) pattern
            final Color bkcolor = style.getFillBackgroundColorColor();
            if (bkcolor != null) {
                if (bkcolor instanceof XSSFColor) {
                    if (((XSSFColor) bkcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                            && (((XSSFColor) bkcolor).getARGBHex() == null
                                    || !((XSSFColor) bkcolor).getARGBHex().equals("FFFFFFFF"))) {
                        return true;
                    }
                }
                if (bkcolor instanceof HSSFColor) {
                    if (((HSSFColor) bkcolor).getIndex() != HSSFColor.HSSFColorPredefined.AUTOMATIC.getIndex()
                            && (((HSSFColor) bkcolor).getHexString() == null
                                    || !((HSSFColor) bkcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                        return true;
                    }
                }
            }

            // Keep cell with a colored (not automatic and not white) background
            final Color fgcolor = style.getFillForegroundColorColor();
            if (fgcolor != null) {
                if (fgcolor instanceof XSSFColor) {
                    if (((XSSFColor) fgcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                            && (((XSSFColor) fgcolor).getARGBHex() == null
                                    || !((XSSFColor) fgcolor).getARGBHex().equals("FFFFFFFF"))) {
                        return true;
                    }
                }
                if (fgcolor instanceof HSSFColor) {
                    if (((HSSFColor) fgcolor).getIndex() != HSSFColor.HSSFColorPredefined.AUTOMATIC.getIndex()
                            && (((HSSFColor) fgcolor).getHexString() != null
                                    || !((HSSFColor) fgcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return true;
        }
    }

    private String getValue(Cell cell) {
        CellType type = cell.getCellType();
        if (type.equals(CellType.FORMULA)) {
            type = cell.getCachedFormulaResultType();
        }

        String value = "";

        switch (cell.getCellType()) {
        case STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                value = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
            } else {
                double d = cell.getNumericCellValue();
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

        // if (type.equals(CellType.BOOLEAN)) {
        // value = cell.getBooleanCellValue() ? "TRUE" : "FALSE";
        // } else if (type.equals(CellType.STRING)) {
        // value = cell.getStringCellValue();
        // } else if (type.equals(CellType.NUMERIC)) {
        // try {
        // value = this.formatter.formatCellValue(cell, evaluator);
        // if (value.matches("-?\\d+")) {
        // double d = cell.getNumericCellValue();
        // if (d != Math.rint(d)) {
        // value = String.valueOf(d);
        // }
        // }
        // } catch (NotImplementedException x) {
        // }
        // }

        return value;
    }

    public int getMergeAcross(Cell cell) {
        if (this.cachedRegion.size() == 0) {
            return 1;
        }
        if (cell == null) {
            return 1;
        }

        int numberOfCells = 1;
        for (CellRangeAddress region : this.cachedRegion) {
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
                break;
            }
        }

        return numberOfCells;
    }

    private int getMergeDown(Row row) {
        int result = 0;

        if (this.cachedRegion.size() == 0) {
            return 0;
        }

        Iterator<Cell> it = row.cellIterator();
        while (it.hasNext()) {
            Cell cell = it.next();
            for (CellRangeAddress region : this.cachedRegion) {
                if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                    if ((cell.getRowIndex() > region.getFirstRow()) && (region.getLastRow() > region.getFirstRow())) {
                        result++;
                    }
                }
            }
        }

        return result;
    }

    private Sheet sheet;
    private RowTranslator rowTranslator;
    private ArrayList<CellRangeAddress> cachedRegion;
}
