package com.github.romualdrousseau.any2json;

public abstract class Row implements IRow
{
	public boolean isEmpty(double ratioOfEmptiness) {
		if(getNumberOfCells() == 0) {
            return true;
        }

        try {
            int cellCount = 0;
            int emptyCellCount = 0;
            for(int i = 0; i < getNumberOfCells();) {
                String value = getCellValueAt(i);
                if(value == null || value.trim().equals("")) {
                    emptyCellCount++;
                }
                cellCount++;
                i += getNumberOfMergedCellsAt(i);
            }

            double emptiness = Double.valueOf(emptyCellCount) / Double.valueOf(cellCount);

            return emptiness >= ratioOfEmptiness;
        }
        catch(UnsupportedOperationException x) {
            return true;
        }
    }
}
