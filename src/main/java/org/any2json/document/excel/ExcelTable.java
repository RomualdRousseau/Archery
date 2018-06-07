package org.any2json.document.excel;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddress;

import org.any2json.ITable;
import org.any2json.TableHeader;
import org.any2json.IRow;
import org.any2json.utility.StringUtility;

class ExcelTable extends ITable
{
	public ExcelTable(Sheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
		m_sheet = sheet;
		m_firstColumn = firstColumn;
		m_firstRow = firstRow + 1;
		m_lastColumn = lastColumn;
		m_lastRow = lastRow;

		processHeaders();
	}

	public int getNumberOfColumns() {
		return m_lastColumn - m_firstColumn + 1;	
	}

	public int getNumberOfRows() {
		return m_lastRow - m_firstRow + 1;	
	}

	public IRow getRowAt(int i) {
		Row row = m_sheet.getRow(m_firstRow + i);
		return (row != null ) ? new ExcelRow(row, m_formatter, m_firstColumn, m_lastColumn) : null;	
	}

	protected ITable addRow(IRow row) {
		return this;
	}

	private void processHeaders() {
		Row headerRow = m_sheet.getRow(m_firstRow - 1);
		int ignoreCells = 0;
		for(Cell cell: headerRow) {
			if(ignoreCells > 0) {
				ignoreCells--;
				continue;
			}
	
			TableHeader header = new TableHeader();
			header.columnIndex = cell.getColumnIndex();
			header.numberOfCells = findNumberOfCells(cell);
			header.name = StringUtility.removeWhiteSpaces(m_formatter.formatCellValue(cell)).replaceAll("\\(.*\\)", "");
			header.tag = null;
			addHeader(header);

			ignoreCells = header.numberOfCells - 1;
		}
	}

	private int findNumberOfCells(Cell cell) {
		int numberOfCells = 1;
		for(int i = 0; i < m_sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = m_sheet.getMergedRegion(i);
			if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
			}
		}
		return numberOfCells;
	}

	private Sheet m_sheet = null;
	private DataFormatter m_formatter = new DataFormatter();
	private int m_firstColumn;
	private int m_firstRow;
	private int m_lastColumn;
	private int m_lastRow;
}