package com.github.romualdrousseau.any2json.loader.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;
import com.github.romualdrousseau.shuju.types.Tensor;

class CsvSheet extends PatcheableSheetStore implements Closeable {

    private static final int BATCH_SIZE = 100000;
    private static final String[] SEPARATORS = { "\t", ",", ";" };

    private final String name;
    private BufferedReader reader;
    private DataFrame rows;

    public CsvSheet(final String name, final BufferedReader reader) {
        this.name = name;
        this.reader = reader;
        this.rows = null;
    }

    @Override
    public void close() throws IOException {
        if (this.rows != null) {
            this.rows.close();
        }
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        return (this.rows != null) ? this.rows.getRow(rowIndex).size() - 1 : 0;
    }

    @Override
    public int getLastRowNum() {
        return (this.rows != null) ? this.rows.getRowCount() - 1 : 0;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return true;
        } else {
            return this.getCellAt(colIndex, rowIndex) != null;
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return patchCell;
        } else {
            return this.getCellAt(colIndex, rowIndex);
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
    }

    public CsvSheet ensureDataLoaded() {
        if (this.rows != null) {
            return this;
        }
        try (final var writer = new DataFrameWriter(BATCH_SIZE);) {
            this.rows = this.processRows(this.reader, writer);
            this.reader.close();
            this.reader = null;
            return this;
        } catch (final IOException x) {
            return this;
        }
    }

    public void checkDataEncoding() throws IOException {
        this.reader.mark(0);
        final var textRow = this.reader.readLine();
        if (textRow != null) {
            final var separator = this.guessSeparator(textRow);
            final var cells = parseOneRow(textRow, separator);
            if (!this.checkIfGoodEncoding(cells)) {
                throw new IOException("CSV bad encoding");
            }
        }
        this.reader.reset();
    }

    private DataFrame processRows(final BufferedReader reader, final DataFrameWriter writer) throws IOException {
        var firstPass = true;
        var separator = ",";
        for (String textRow; (textRow = reader.readLine()) != null;) {

            if (firstPass) {
                separator = this.guessSeparator(textRow);
                firstPass = false;
            }

            final String[] cells = parseOneRow(textRow, separator);
            for (int j = 0; j < cells.length; j++) {
                cells[j] = StringUtils.cleanToken(cells[j]);
            }

            writer.write(Row.of(cells));
        }

        return writer.getDataFrame();
    }

    private String[] parseOneRow(final String data, final String separator) {
        final var result = new ArrayList<String>();
        var acc = "";
        var state = 0;

        final char[] tmp = data.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            final char c = tmp[i];

            switch (state) {
                case 0:
                    if (c == separator.charAt(0)) {
                        result.add(acc);
                        acc = "";
                    } else if (c == '"' && acc.trim().equals("")) {
                        acc += c;
                        state = 1;
                    } else {
                        acc += c;
                    }
                    break;

                case 1: // Double quote context
                    if (c == '"') {
                        acc += c;
                        state = 2;
                    } else {
                        acc += c;
                    }
                    break;

                case 2: // Check double quote context exit
                    if (c == '"') {
                        state = 1;
                    } else if (c == separator.charAt(0)) {
                        result.add(acc);
                        acc = "";
                        state = 0;
                    } else {
                        acc += c;
                        state = 0;
                    }
                    break;
            }
        }

        if (!acc.trim().equals("")) {
            result.add(acc);
        }

        return result.toArray(String[]::new);
    }

    private boolean checkIfGoodEncoding(final String[] row) {
        boolean result = true;
        for (int i = 0; i < row.length; i++) {
            result &= StringUtils.checkIfGoodEncoding(row[i]);
        }
        return result;
    }

    private String guessSeparator(final String sample) {
        // find the separator generating the more of columns
        final float[] v = new float[SEPARATORS.length];
        for (int i = 0; i < SEPARATORS.length; i++) {
            v[i] = sample.split(SEPARATORS[i], -1).length;
        }
        return SEPARATORS[(int) Tensor.of(v).argmax(0).item(0)];
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
        if(rowIndex >= this.rows.getRowCount()) {
            return null;
        }

        final Row row = this.rows.getRow(rowIndex);

        if(colIndex >= row.size()) {
            return null;
        }

        return row.get(colIndex);
    }
}
