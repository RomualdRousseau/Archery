package com.github.romualdrousseau.any2json.document.excel;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.DataFormatter;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.util.StringUtility;

class ExcelTable extends Table
{
	public ExcelTable(Sheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
		this.sheet = sheet;
		this.formatter = new DataFormatter();
		this.firstColumn = firstColumn;
		this.firstRow = firstRow + 1;
		this.lastColumn = lastColumn;
		this.lastRow = lastRow;

		processHeaders();
	}

	public int getNumberOfColumns() {
		return this.lastColumn - this.firstColumn + 1;	
	}
	
	public int getNumberOfRows() {
		return this.lastRow - this.firstRow + 1;	
	}

	public Row getRowAt(int i) {
		if(i < 0 || i >= getNumberOfRows()) {
			throw new ArrayIndexOutOfBoundsException(i);
		}

		org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(this.firstRow + i);
		return (row != null ) ? new ExcelRow(row, this.formatter, this.firstColumn, this.lastColumn) : null;	
	}

	private void processHeaders() {
		int ignoreCells = 0;
		for(Cell cell: this.sheet.getRow(this.firstRow - 1)) {
			if((cell.getColumnIndex() - this.firstColumn) >= getNumberOfColumns()) {
				break;
			}

			if(ignoreCells > 0) {
				ignoreCells--;
				continue;
			}

			TableHeader header = new TableHeader()
				.setColumnIndex(cell.getColumnIndex() - this.firstColumn)
				.setNumberOfCells(findNumberOfCells(cell))
				.setName(StringUtility.cleanValueToken(this.formatter.formatCellValue(cell)))
				.setTag(null);
			addHeader(header);
			
			ignoreCells = header.getNumberOfCells() - 1;
		}
	}

	private int findNumberOfCells(Cell cell) {
		int numberOfCells = 1;
		for(int i = 0; i < this.sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = this.sheet.getMergedRegion(i);
			if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
			}
		}
		return numberOfCells;
	}

	private Sheet sheet;
	private DataFormatter formatter;
	private int firstColumn;
	private int firstRow;
	private int lastColumn;
	private int lastRow;
}