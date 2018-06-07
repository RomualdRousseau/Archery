package org.any2json.document.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddress;

import org.shuju.cv.ISearchBitmap;
import org.shuju.cv.Template;
import org.shuju.cv.Filter;

import org.any2json.utility.StringUtility;

public class ExcelSearchBitmap extends ISearchBitmap
{
	public ExcelSearchBitmap(Sheet sheet, int columns, int rows) {
		this.width = columns;
		this.height = rows;
		this.data = new int[this.height][this.width];

		loadData(sheet);

		new Filter(new Template(new int[][] {{1, 1, 1, 1, 1}})).apply(this, 3);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int get(int x, int y) {
		if(x < 0 || x >= this.width || y < 0 || y >= this.height) {
			return 0;
		}
		return this.data[y][x];
	}

	public void set(int x, int y, int v) {
		this.data[y][x] = v;
	}

	private void loadData(Sheet sheet) {
		for(int y = 0; y < this.height; y++) {
			for(int x = 0; x < this.width; x++) {
				this.data[y][x] = getInternalCellValueAt(sheet, x, y);
			}
		}
	}

	private int getInternalCellValueAt(Sheet sheet, int colIndex, int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		if(row == null) {
			return 0;
		}

		Cell cell = row.getCell(colIndex);
		if(cell == null) {
			return 0;
		}

		CellRangeAddress region = checkIfMergedCell(sheet, cell);
		if(region != null) {
			cell = row.getCell(region.getFirstColumn());
		}

		return (cell == null) ?  0 : 1;
	}

	private CellRangeAddress checkIfMergedCell(Sheet sheet,Cell cell) {
		for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				return region;
			}
		}
		return null;
	}

	private int width;
	private int height;
	private int[][] data;
}