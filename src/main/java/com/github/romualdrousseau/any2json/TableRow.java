package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.util.StringUtility;

public abstract class TableRow implements IRow {
    public boolean isEmpty(double ratioOfEmptiness) {
        if (getNumberOfCells() == 0) {
            return true;
        }

        try {
            int cellCount = 0;
            int emptyCellCount = 0;
            for (int i = 0; i < getNumberOfCells();) {
                String value = getCellValueAt(i);
                if (StringUtility.isEmpty(value)) {
                    emptyCellCount++;
                }
                cellCount++;
                i += getNumberOfMergedCellsAt(i);
            }

            double emptiness = Double.valueOf(emptyCellCount) / Double.valueOf(cellCount);

            return emptiness >= ratioOfEmptiness;
        } catch (UnsupportedOperationException x) {
            return true;
        }
    }

    public TableCell getCell(TableHeader header) {
        return this.getCell(header, false);
    }

    public TableCell getCell(TableHeader header, boolean mergeValues) {
        return new TableCell(header).setValue(this.getCellValue(header, mergeValues));
    }

    public String getCellValue(TableHeader header, boolean mergeValues) {
        if (mergeValues) {
            String result = "";
            for(TableHeader current = header; current != null; current = current.next()) {
                String value = getCellValue(current);
                if(value.contains(result)) {
                    result = value;
                }
                else if (!result.contains(value)) {
                    result += value;
                }
            }
            return result;
        } else {
            return getCellValue(header);
        }
    }
}
