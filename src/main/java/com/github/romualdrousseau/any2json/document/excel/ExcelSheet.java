package com.github.romualdrousseau.any2json.document.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.ISheet;

class ExcelSheet implements ISheet
{
	public ExcelSheet(Sheet sheet) {
		this.sheet = sheet;
		this.table = null;
	}

	public String getName() {
		return this.sheet.getSheetName();
	}

	public ITable getTable() {
		int lastColumnNum = estimateLastColumnNum();
		if(this.table == null && lastColumnNum > 0) {
			this.table = new ExcelTable(this.sheet, 0, 0, lastColumnNum, this.sheet.getLastRowNum());
		}
		return this.table;
	}

	public ExcelTable findTable(int headerColumns, int headerRows) {
		if(this.table == null) {
			ExcelSearchBitmap searchBitmap = new ExcelSearchBitmap(this.sheet, headerColumns, headerRows);
			SearchPoint[] table = new RectangleExtractor().extractBest(searchBitmap);
			//debug(searchBitmap, table);
			if(table != null && table[1].getX() > table[0].getX()) {
				this.table = new ExcelTable(this.sheet, table[0].getX(), table[0].getY(), table[1].getX(), this.sheet.getLastRowNum());
			}
		}
		return this.table;
	}

	private int estimateLastColumnNum() {
		Row row = this.sheet.getRow(0);
		if(row == null) {
			return 0;
		}
		int colNum = 0;
		Cell cell = row.getCell(colNum);
		while(cell != null) {
			cell = row.getCell(++colNum);
		}
		return colNum;
	}

	private void debug(ExcelSearchBitmap searchBitmap, SearchPoint[] table) {
		for(int i = 0; i < searchBitmap.getHeight(); i++) {
			for(int j = 0; j < searchBitmap.getWidth(); j++) {
				if(searchBitmap.get(j, i) == 0) {
					System.out.print("_");
				}
				else {
					System.out.print("#");
				}
			}
			System.out.println();
		}

		if(table != null) {
			System.out.print(table[0].getX() + " ");
			System.out.print(table[0].getY() + " ");
			System.out.print(table[1].getX() + " ");
			System.out.println(table[1].getY());
		}
	}

	private Sheet sheet;
	private ExcelTable table;
}
