package com.github.romualdrousseau.archery.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class PatcheableSheetStore implements SheetStore {

    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2, final String value, final boolean unmergeAll) {
        final String newCell;
        if (value == null) {
            newCell = this.getCellDataAt(colIndex1, rowIndex1);
        } else {
            newCell = value;
        }
        this.addPatchCell(colIndex2, rowIndex2, newCell);
    }

    public String getPatchCell(final int colIndex, final int rowIndex) {
        final var patchCells = patchRows.get(rowIndex);
        if (patchCells != null && colIndex < patchCells.size() && patchCells.get(colIndex) != null) {
            return patchCells.get(colIndex);
        } else {
            return null;
        }
    }

    public void addPatchCell(final int colIndex, final int rowIndex, final String value) {
        var cells = patchRows.get(rowIndex);
        if (cells != null && colIndex < cells.size()) {
            cells.set(colIndex, value);
        } else {
            if (cells == null) {
                cells = new ArrayList<>();
                patchRows.put(rowIndex, cells);
            }
            for(int i = cells.size(); i < colIndex; i++) {
                cells.add(null);
            }
            cells.add(value);
        }
    }

    private final HashMap<Integer, List<String>> patchRows = new HashMap<>();
}
