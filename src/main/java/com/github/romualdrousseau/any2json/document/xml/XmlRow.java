package com.github.romualdrousseau.any2json.document.xml;

import com.github.romualdrousseau.any2json.IHeader;
import com.github.romualdrousseau.any2json.TableRow;
import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;

public class XmlRow extends TableRow
{
	public XmlRow(XmlTable table, Row row, int groupId) {
        this.table = table;
        this.row = row;
        this.groupId = groupId;
    }

    public XmlTable getTable() {
        return this.table;
    }

	public int getNumberOfCells() {
		return this.table.getNumberOfColumns();
    }

    public int getNumberOfMergedCellsAt(int i) {
        Cell cell = this.row.getCellAt(this.table.getFirstColumn() + i + 1);
		if(cell == null) {
			return 1;
        }

		int numberOfCells = cell.getMergeAcross() + 1;

		return numberOfCells;
    }

    public String getCellValueAt(int i) {
		if(i < 0 || i >= getNumberOfCells()) {
			throw new ArrayIndexOutOfBoundsException(i);
        }

        return getInternalCellValueAt(i);
    }

	public String getCellValue(IHeader header) {
		if(header == null) {
			throw new IllegalArgumentException();
		}

		String result = getInternalCellValueAt(header.getColumnIndex());
		if(result == null) {
			result = "";
		}

		for(int i = 1; i < header.getNumberOfCells(); i++) {
			String s = getInternalCellValueAt(header.getColumnIndex() + i);
			if(s != null) {
				result += s;
			}
		}

		return result;
    }

	private String getInternalCellValueAt(int i) {
		Cell cell = this.row.getCellAt(this.table.getFirstColumn() + i + 1);
		if(!cell.hasData()) {
			return "";
        }

		String type = cell.getXLDataType();
        String value = cell.getData$();

		// TRICKY: Get hidden decimals in case of a rounded numeric value
		if(type == Cell.DATATYPE_NUMBER && value.matches("-?\\d+")) {
			double d = cell.doubleValue();
			value = (Math.floor(d) == d) ? value : String.valueOf(d);
		}
		else if(type == Cell.DATATYPE_ERROR) {
			throw new UnsupportedOperationException("Unexceptected Cell Error at [" + row.getIndex() + ";" + (this.table.getFirstColumn() + i) + "]");
		}

		return StringUtility.cleanToken(value);
    }

    private XmlTable table;
	private Row row;
}
