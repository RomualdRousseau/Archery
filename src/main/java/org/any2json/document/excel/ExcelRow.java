package org.any2json.document.excel;

import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;

import org.any2json.IRow;
import org.any2json.TableHeader;
import org.any2json.utility.StringUtility;

public class ExcelRow extends IRow
{
	public ExcelRow(Row row, DataFormatter formatter, int firstColumn, int lastColumn) {
		m_row = row;
		m_formatter = formatter;
		m_firstColumn = firstColumn;
		m_lastColumn = lastColumn;
	}

	public int getNumberOfCells() {
		return m_lastColumn - m_firstColumn + 1;
	}

	public String getCellValue(TableHeader header) {
		String result = getInternalCellValueAt(header.columnIndex);
		if(result == null) {
			result = "";
		}

		for(int i = 1; i < header.numberOfCells; i++) {
			String s = getInternalCellValueAt(header.columnIndex + i);
			if(s != null) {
				result += StringUtility.trim(s);
			}
		}
		
		return StringUtility.normalizeWhiteSpaces(result);
	}

	public String getCellValueAt(int i) {
		String result = getInternalCellValueAt(i);
		return StringUtility.normalizeWhiteSpaces(StringUtility.trim(result));
	}

	private String getInternalCellValueAt(int i) {
		if(i < 0 || i >= getNumberOfCells()) {
			throw new java.lang.ArrayIndexOutOfBoundsException(i);
		}

		Cell cell = m_row.getCell(m_firstColumn + i);
		if(cell == null) {
			return null;
		}

		String value = m_formatter.formatCellValue(cell);
		if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC && Pattern.matches("-?\\d+", value)) { // !!! DIRTY !!! Check if not a date
			double d = cell.getNumericCellValue();
			if(Math.floor(d) == d) {
				return value;
			}
			else {
				return "" + d; // !!! DIRTY !!! get the right double number
			}
		}
		else {
			return value;
		}
	}

	private Row m_row = null;
	private DataFormatter m_formatter = null;
	private int m_firstColumn;
	private int m_lastColumn;
}