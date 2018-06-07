package org.any2json.document.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;

import org.shuju.cv.SearchPoint;
import org.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

import org.any2json.ISheet;
import org.any2json.ITable;

class ExcelSheet extends ISheet
{
	public ExcelSheet(Sheet sheet, int headerColumns, int headerRows) {
		m_sheet = sheet;
		m_headerColumns = headerColumns;
		m_headerRows = Math.min(headerRows, m_sheet.getLastRowNum() + 1);
		m_table = null;
	}

	public String getName() {
		return m_sheet.getSheetName();
	}

	public ITable getTable() {
		if(m_table == null) {
			m_table = findTable(m_headerColumns, m_headerRows);
		}
		return m_table;
	}

	private ExcelTable findTable(int headerColumns, int headerRows) {
		ExcelSearchBitmap searchBitmap = new ExcelSearchBitmap(m_sheet, headerColumns, headerRows);
		SearchPoint[] table = new RectangleExtractor().extractBest(searchBitmap);
		//debug(searchBitmap, table);
		if(table == null) {
			return null;
		}

		return new ExcelTable(m_sheet, table[0].getX(), table[0].getY(), table[1].getX(), m_sheet.getLastRowNum());
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

	private Sheet m_sheet;
	private int m_headerColumns;
	private int m_headerRows;
	private ExcelTable m_table;
}
