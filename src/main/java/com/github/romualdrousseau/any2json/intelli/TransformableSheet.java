package com.github.romualdrousseau.any2json.intelli;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.any2json.intelli.parser.DataTableGroupSubFooterParserFactory;
import com.github.romualdrousseau.any2json.intelli.parser.DataTableGroupSubHeaderParserFactory;
import com.github.romualdrousseau.any2json.intelli.parser.DataTableParserFactory;
import com.github.romualdrousseau.shuju.util.StringFuzzy;
import com.github.romualdrousseau.shuju.util.StringUtils;

public abstract class TransformableSheet extends BaseSheet {

    public TransformableSheet(final SheetStore store) {
        super(store);
    }

    public DataTableParserFactory getDataTableParserFactory() {
        return this.dataTableParserFactory;
    }

    public void setDataTableParserFactory(String dataTableParserFactoryAsString) {
        if (dataTableParserFactoryAsString.equals("DataTableGroupSubHeaderParserFactory")) {
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        }
        else if (dataTableParserFactoryAsString.equals("DataTableGroupSubFooterParserFactory")) {
            this.dataTableParserFactory = new DataTableGroupSubFooterParserFactory();
        }
        else { // Default to DataTableGroupSubHeaderParserFactory
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        }
    }

    public void stichRows() {
        for(int i = 0; i <= this.getLastRowNum(); i++) {
            if (this.isStichedRow(i)) {
                this.markRowAsNull(i);
            }
        }
        this.removeAllNullRows();
    }

    public void dropColumn(final int colIndex) {
        this.markColumnAsNull(colIndex);
        this.removeAllNullColumns();
    }

    public void dropNullColumns(final float fillRatio) {
        for(int j = 0; j <= this.getLastColumnNum(); j++) {
            int emptyCount = this.getLastRowNum() + 1;
            for(int i = 0; i <= this.getLastRowNum(); i++) {
                if(this.hasCellDataAt(j, i) && !StringUtils.isFastBlank(this.getCellDataAt(j, i))) {
                    emptyCount--;
                }
            }
            final float m = 1.0f - (float) emptyCount / (float) (this.getLastRowNum() + 1);
            if (m <= fillRatio) {
                this.markColumnAsNull(j);
            }
        }
        this.removeAllNullColumns();
    }

    public void dropRow(final int rowIndex) {
        this.markRowAsNull(rowIndex);
        this.removeAllNullRows();
    }

    public void dropNullRows(final float fillRatio) {
        for(int i = 0; i <= this.getLastRowNum(); i++) {
            int emptyCount = this.getLastColumnNum() + 1;
            for(int j = 0; j <= this.getLastColumnNum(i); j++) {
                if(this.hasCellDataAt(j, i) && !StringUtils.isFastBlank(this.getCellDataAt(j, i))) {
                    emptyCount--;
                }
            }
            final float m = 1.0f - (float) emptyCount / (float) (this.getLastColumnNum() + 1);
            if (m <= fillRatio) {
                this.markRowAsNull(i);
            }
        }
        this.removeAllNullRows();
    }

    public void mergeCell(final int colIndex) {
        int lastRow = -1;
        for(int i = 0; i <= this.getLastRowNum(); i++) {
            if(this.hasCellDataAt(colIndex, i)) {
                lastRow = i;
            } else if (lastRow >= 0) {
                this.patchCell(colIndex, lastRow, colIndex, i, null);
            }
        }
    }

    private boolean isStichedRow(final int rowIndex) {
        if (rowIndex <= 0 || rowIndex >= this.getSheetStore().getLastRowNum()) {
            return false;
        }
        final String hash = this.getRowPattern(rowIndex);
        // Keep non empty rows
        if (!hash.isEmpty()) {
            return false;
        }
        // Test if the previous and next rows can be "stiched"
        final String hashPrev = this.getRowPattern(rowIndex - 1);
        final String hashNext = this.getRowPattern(rowIndex + 1);
        return StringFuzzy.Hamming(hashPrev, hashNext) >= DocumentFactory.DEFAULT_RATIO_SIMILARITY;
    }

    private String getRowPattern(final int rowIndex) {
        String hash = "";
        int countEmptyCells = 0;
        for (int i = 0; i <= this.getSheetStore().getLastColumnNum(rowIndex);) {
            final String value = this.getSheetStore().getCellDataAt(i, rowIndex);
            if (value != null) {
                if (value.isEmpty()) {
                    hash += "s";
                    countEmptyCells++;
                }
                else {
                    hash += this.getClassifierFactory().getLayoutClassifier().map(c -> c.toEntityValue(value).map(x -> "e").orElse("v")).orElse("v");
                }
            }
            i += this.getSheetStore().getNumberOfMergedCellsAt(i, rowIndex);
        }
        if (countEmptyCells == hash.length()) {
            hash = "";
        }
        return hash;
    }

    private DataTableParserFactory dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
}
