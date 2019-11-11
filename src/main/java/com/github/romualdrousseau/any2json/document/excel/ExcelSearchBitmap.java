package com.github.romualdrousseau.any2json.document.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

public class ExcelSearchBitmap implements ISearchBitmap
{
    public ExcelSearchBitmap(int columns, int rows) {
        this.width = columns;
		this.height = rows;
		this.data = new int[this.height][this.width];
    }

	public ExcelSearchBitmap(Sheet sheet, int columns, int rows) {
		this.width = columns;
		this.height = rows;
		this.data = new int[this.height][this.width];
		loadData(sheet);
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

    public ISearchBitmap clone() {
        ExcelSearchBitmap result = new ExcelSearchBitmap(width, height);
        for(int y = 0; y < this.height; y++) {
			for(int x = 0; x < this.width; x++) {
                result.data[y][x] = this.data[y][x];
			}
        }
        return result;
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

        int firstColumn = checkIfMergedCell(sheet, cell);
        if (firstColumn >= 0) {
            cell = row.getCell(firstColumn);
        }

		return (!checkIfCellHasData(cell)) ? 0 : 1;
    }

    private boolean checkIfCellHasData(Cell cell) {
        return cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK || cell.getCellStyle().getFillBackgroundColorColor() != null);
    }

	private int checkIfMergedCell(Sheet sheet, Cell cell) {
		for(int i = 0; i < Math.min(sheet.getNumMergedRegions(), 100); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				return region.getFirstColumn();
			}
		}
		return -1;
	}

	private int width;
	private int height;
	private int[][] data;
}
