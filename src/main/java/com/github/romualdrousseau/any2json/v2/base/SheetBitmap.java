package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class SheetBitmap implements ISearchBitmap
{
    public SheetBitmap(int columns, int rows) {
        this.width = columns;
		this.height = rows;
		this.data = new byte[this.height][this.width];
    }

	public SheetBitmap(Sheet sheet, int columns, int rows) {
		this.width = columns;
		this.height = rows;
        this.data = new byte[this.height][this.width];
        this.convertSheetToBitmap(sheet);
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
		this.data[y][x] = (byte) v;
    }

    public ISearchBitmap clone() {
        SheetBitmap result = new SheetBitmap(width, height);
        for(int y = 0; y < this.height; y++) {
			for(int x = 0; x < this.width; x++) {
                result.data[y][x] = this.data[y][x];
			}
        }
        return result;
    }

    private void convertSheetToBitmap(Sheet sheet) {
        for(int y = 0; y < this.height; y++) {
			for(int x = 0; x < this.width;) {
                final int n = sheet.getNumberOfMergedCellsAt(x, y);
                if (!StringUtility.isFastEmpty(sheet.getInternalCellValueAt(x, y))) {
                    for(int k = 0; k < n; k++) {
                        this.data[y][x + k] = 1;
                    }
                }
                x += n;
			}
        }
    }

	private int width;
	private int height;
	private byte[][] data;
}
