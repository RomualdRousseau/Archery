package com.github.romualdrousseau.any2json.v2.loader.excel.xml;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

class XmlSheet extends IntelliSheet {

    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(int rowIndex) {
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return 0;
        }

        return row.maxCellIndex();
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows().size() - this.getRowTranslator().getIgnoredRowCount() - 1;
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        return cell != null;
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (cell == null) {
            return null;
        }
        return StringUtility.cleanToken(cell.getData$());
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (cell == null) {
            return 1;
        }
        return cell.getMergeAcross() + 1;
    }

    @Override
    protected String getRowHash(int rowIndex, ITagClassifier classifier) {
        if (rowIndex < 0 || rowIndex >= this.sheet.getRows().size()) {
            return "";
        }

        Row row = this.sheet.getRowAt(rowIndex + 1);
        if (row == null || row.getCells().size() == 0) {
            return "";
        }

        String hash = "";
        int countEmptyCells = 0;
        boolean checkIfRowMergedVertically = false;
        int colIndex = 0;
        for (Cell cell : row.getCells()) {
            if (cell.hasData()) {
                String value = cell.getData$();
                if (value.isEmpty()) {
                    hash += "s";
                    countEmptyCells++;
                } else if (classifier != null) {
                    Vector v = classifier.getEntityList().word2vec(value);
                    if (v.sparsity() < 1.0f) {
                        hash += "e";
                    } else {
                        hash += "v";
                    }
                } else {
                    hash += "v";
                }
            }

            if (!checkIfRowMergedVertically && this.getMergeDown(colIndex, rowIndex) > 0) {
                checkIfRowMergedVertically = true;
            }

            colIndex++;
        }

        if (checkIfRowMergedVertically) {
            hash = "X";
        } else if (countEmptyCells == hash.length()) {
            hash = "";
        }

        return hash;
    }

    private Row getRowAt(int rowIndex) {
        final int translatedRow = this.getRowTranslator().rebase(rowIndex);
        if (translatedRow == -1) {
            return null;
        }
        Row row = this.sheet.getRowAt(translatedRow + 1);
        if (row == null) {
            return null;
        }
        return row;
    }

    private Cell getCellAt(int colIndex, int rowIndex) {
        final int translatedRow = this.getRowTranslator().rebase(rowIndex);
        if (translatedRow == -1) {
            return null;
        }
        Cell cell = this.sheet.getCellAt(translatedRow + 1, colIndex + 1);
        if (!cell.hasData()) {
            return null;
        }
        return cell;
    }

    private int getMergeDown(int colIndex, int rowIndex) {
        if (rowIndex <= 0) {
            return 0;
        }

        int numberOfCells = 0;
        for (int i = 1; i < 5; i++) {
            int firstRow = rowIndex - i;
            if (firstRow < 0) {
                break;
            }

            int lastRow = firstRow + this.sheet.getCellAt(firstRow + 1, colIndex + 1).getMergeDown();
            if (lastRow > firstRow && firstRow <= rowIndex && rowIndex <= lastRow) {
                numberOfCells = lastRow - firstRow;
                break;
            }
        }

        return numberOfCells;
    }

    private Worksheet sheet;
}
