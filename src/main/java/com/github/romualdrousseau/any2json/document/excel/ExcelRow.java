package com.github.romualdrousseau.any2json.document.excel;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.util.StringUtility;

public class ExcelRow extends Row
{
	public ExcelRow(org.apache.poi.ss.usermodel.Row row, FormulaEvaluator evaluator, DataFormatter formatter, int firstColumn, int lastColumn) {
		this.row = row;
		this.evaluator = evaluator;
		this.formatter = formatter;
		this.firstColumn = firstColumn;
		this.lastColumn = lastColumn;
	}

	public int getNumberOfCells() {
		return this.lastColumn - this.firstColumn + 1;
	}

	public String getCellValue(TableHeader header) {
		if(header == null) {
			throw new IllegalArgumentException();
		}

		String result = getCellValueAt(header.getColumnIndex());
		if(result == null) {
			result = "";
		}

		for(int i = 1; i < header.getNumberOfCells(); i++) {
			String s = getCellValueAt(header.getColumnIndex() + i);
			if(s != null) {
				result += s;
			}
		}

		return result;
	}

	public String getCellValueAt(int i) {
		if(i < 0 || i >= getNumberOfCells()) {
			throw new ArrayIndexOutOfBoundsException(i);
		}

		Cell cell = this.row.getCell(this.firstColumn + i);
		if(cell == null) {
			return null;
		}

		int type = evaluator.evaluateInCell(cell).getCellType();

		//int type = cell.getCellType();
		//if(type == Cell.CELL_TYPE_FORMULA) {
		//	type = evaluator.evaluateInCell(cell).getCellType();
		//}

		String value = this.formatter.formatCellValue(cell);

		// TRICKY: Get hidden decimals in case of a rounded numeric value
		if(type == Cell.CELL_TYPE_NUMERIC && value.matches("-?\\d+")) {
			double d = cell.getNumericCellValue();
			value = (Math.floor(d) == d) ? value : String.valueOf(d);
		}
		else if(type == Cell.CELL_TYPE_ERROR) {
			throw new UnsupportedOperationException("Unexceptected Cell Error at [" + row.getRowNum() + ";" + (this.firstColumn + i) + "]");
		}

		return StringUtility.cleanValueToken(value);
	}

	private org.apache.poi.ss.usermodel.Row row;
	private FormulaEvaluator evaluator;
	private DataFormatter formatter;
	private int firstColumn;
	private int lastColumn;
}
