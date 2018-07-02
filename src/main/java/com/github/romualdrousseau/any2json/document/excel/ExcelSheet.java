package com.github.romualdrousseau.any2json.document.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;

import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.ISheet;

class ExcelSheet implements ISheet
{
	public ExcelSheet(Sheet sheet, int headerColumns, int headerRows) {
		this.sheet = sheet;
		this.headerColumns = headerColumns;
		this.headerRows = Math.min(headerRows, this.sheet.getLastRowNum() + 1);
		this.table = null;
	}

	public String getName() {
		return this.sheet.getSheetName();
	}

	public ITable getTable() {
		if(this.table == null) {
			this.table = findTable(this.headerColumns, this.headerRows);
		}
		return this.table;
	}

	private ExcelTable findTable(int headerColumns, int headerRows) {
		ExcelSearchBitmap searchBitmap = new ExcelSearchBitmap(this.sheet, headerColumns, headerRows);
		SearchPoint[] table = new RectangleExtractor().extractBest(searchBitmap);
		//debug(searchBitmap, table);
		if(table == null) {
			return null;
		}

		return new ExcelTable(this.sheet, table[0].getX(), table[0].getY(), table[1].getX(), this.sheet.getLastRowNum());
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
	private int headerColumns;
	private int headerRows;
	private ExcelTable table;
}
