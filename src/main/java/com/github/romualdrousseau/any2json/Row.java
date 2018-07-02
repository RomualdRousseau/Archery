package com.github.romualdrousseau.any2json;

public abstract class Row implements IRow
{
	public boolean isEmpty(double ratioOfEmptiness) {
		if(getNumberOfCells() == 0) {
            return true;
        }

        int emptyCellCount = 0;            
        for(int i = 0; i < getNumberOfCells(); i++) {
        	String value = getCellValueAt(i);
        	if(value == null || value.equals("")) {
                emptyCellCount++;
            }
        }
        double emptiness = Double.valueOf(emptyCellCount) / Double.valueOf(getNumberOfCells());

        return emptiness >= ratioOfEmptiness;
	}
}