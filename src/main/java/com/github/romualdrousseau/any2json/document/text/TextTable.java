package com.github.romualdrousseau.any2json.document.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

class TextTable extends Table {
    public final static int ROWS_IN_MEMORY = 100000;

    public TextTable(BufferedReader reader, int rowCount) throws IOException {
        processOneTable(reader);
        if (this.rows.size() == 0) {
            return;
        }

        this.buildDataTable(0, 0, this.rows.get(0).getNumberOfCells(), this.rows.size() - 1, 0);

        this.enableMetaTable(false);
    }

    protected TextRow getInternalRowAt(int i) {
        return (i < this.rows.size()) ? this.rows.get(i) : null;
    }

    protected TextTable createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        throw new RuntimeException("Table doesn't support metatable");
    }

    private void processOneTable(BufferedReader reader) throws IOException {
        this.rows = new ArrayList<TextRow>();
        if (reader == null) {
            return;
        }
        processRows(reader);
    }

    private void processRows(BufferedReader reader) throws IOException {
        boolean firstPass = true;
        for (String textRow; (textRow = reader.readLine()) != null;) {

            if (firstPass) {
                this.separator = this.guessSeparator(textRow);
                firstPass = false;
            }

            String[] tokens = parseOneRow(textRow);

            String[] cells = new String[tokens.length];
            for (int j = 0; j < tokens.length; j++) {
                cells[j] = StringUtility.cleanToken(tokens[j]);
            }

            this.rows.add(new TextRow(this, cells, this.lastGroupId));
            // this.processedCount++;

            if (this.rows.size() >= ROWS_IN_MEMORY) {
                return;
            }
        }
    }

    private String[] parseOneRow(String data) {
        ArrayList<String> result = new ArrayList<String>();
        String acc = "";
        int state = 0;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);

            switch (state) {
            case 0:
                if (c == separator.charAt(0)) {
                    result.add(acc);
                    acc = "";
                } else if (c == '"' && acc.trim().equals("")) {
                    acc = "";
                    state = 1;
                } else {
                    acc += c;
                }
                break;

            case 1: // Double quote context
                if (c == '"') {
                    state = 2;
                } else {
                    acc += c;
                }
                break;

            case 2: // Check double quote context exit
                if (c == '"') {
                    acc += c;
                    state = 1;
                } else {
                    result.add(acc);
                    acc = "";
                    state = 0;
                }
                break;
            }
        }

        if (!acc.trim().equals("")) {
            result.add(acc);
        }

        return result.toArray(new String[result.size()]);
    }

    private String guessSeparator(String sample) {
        final String[] separators = { "\t", ",", ";" };

        // find the separator generating the more of columns
        float[] v = new float[separators.length];
        for (int i = 0; i < separators.length; i++) {
            v[i] = sample.split(separators[i]).length;
        }
        return separators[new Vector(v).argmax()];
    }

    // private void ensureRowsInMemory(int i) {
    // try {
    // if (i >= this.processedCount) {
    // processRows(this.reader);
    // }
    // } catch (IOException x) {
    // throw new ArrayIndexOutOfBoundsException(i);
    // }
    // }

    private ArrayList<TextRow> rows;
    private String separator = null;
    // private int processedCount = 0;
    // private int rowCount;
}
