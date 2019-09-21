package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.util.StringUtility;

public abstract class TableRow implements IRow {
    public int getGroupId() {
        return this.groupId;
    }

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

    public TableCell getCellAt(int index) {
        return getCell(getTable().getHeaderAt(index));
    }

    public TableCell getCell(IHeader header) {
        return this.getCell(header, false);
    }

    public TableCell getCell(IHeader header, boolean mergeValues) {
        return new TableCell(header).setValue(this.getCellValue(header, mergeValues));
    }

    public String getCellValue(IHeader header, boolean mergeValues) {
        if (mergeValues) {
            String result = "";
            for(IHeader current = header; current != null; current = current.next()) {
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

    protected int groupId;
}
