package com.github.romualdrousseau.any2json;

public interface IRow
{
	public boolean isEmpty(double ratio);

    public int getNumberOfCells();

    public int getNumberOfMergedCellsAt(int i);

    public TableCell getCell(TableHeader header);

	public String getCellValue(TableHeader header);

    public String getCellValueAt(int i);
}
