package com.github.romualdrousseau.any2json.document.text;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

class TextTable extends Table {
    public final static int ROWS_IN_MEMORY = 100000;

    private int rowCount;

    public TextTable(BufferedReader reader, int rowCount) throws IOException {
        this.reader = reader;
        this.rowCount = rowCount - 1;
        processOneTable();
    }

    public int getNumberOfColumns() {
        return getNumberOfHeaders();
    }

    public int getNumberOfRows() {
        return this.rowCount;
    }

    public IRow getRowAt(int i) {
        if (i < 0 || i >= this.rowCount) {
            throw new ArrayIndexOutOfBoundsException(i);
        }

        ensureRowsInMemory(i);

        return this.rows.get(i % ROWS_IN_MEMORY);
    }

    private void processOneTable() throws IOException {

        if (this.reader == null) {
            return;
        }

        if (!processHeaders(this.reader.readLine())) {
            return;
        }

        processRows(this.reader);
    }

    private boolean processHeaders(String textLine) {

        if (textLine == null) {
            return false;
        }

        this.separator = this.guessSeparator(textLine);

        String[] textHeaders = parseOneRow(textLine);
        for (int i = 0; i < textHeaders.length; i++) {
            addHeader(new TableHeader().setColumnIndex(i).setNumberOfCells(1)
                    .setName(StringUtility.cleanToken(textHeaders[i])).setTag(null));
        }

        return true;
    }

    private void processRows(BufferedReader reader) throws IOException {

        this.rows.clear();

        for (String textRow; (textRow = reader.readLine()) != null;) {

            String[] tokens = parseOneRow(textRow);

            String[] cells = new String[getNumberOfColumns()];
            for (int j = 0; j < Math.min(tokens.length, cells.length); j++) {
                cells[j] = StringUtility.cleanToken(tokens[j]);
            }

            this.rows.add(new TextRow(cells));
            processedCount++;

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

    private void ensureRowsInMemory(int i) {
        try {
            if (i >= this.processedCount) {
                processRows(this.reader);
            }
        } catch (IOException x) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
    }

    private BufferedReader reader;
    private ArrayList<IRow> rows = new ArrayList<IRow>();
    private int processedCount = 0;
    private String separator = null;
}
