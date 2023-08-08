package com.github.romualdrousseau.any2json.loader.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.parser.sheet.SemiStructuredSheetBitmapParser;
import com.github.romualdrousseau.any2json.intelli.parser.sheet.StructuredSheetParser;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.types.Tensor;
import com.github.romualdrousseau.shuju.util.StringUtils;

public class CsvDocument implements Document {

    @Override
    public boolean open(final File txtFile, final String encoding, final String password, final boolean wellFormed) {
        this.wellFormed = wellFormed;

        if (encoding != null && this.openWithEncoding(txtFile, encoding)) {
            return true;
        } else if (encoding != null) {
            return this.openWithEncoding(txtFile, "UTF-8");
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        this.sheet = null;
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public Sheet getSheetAt(final int i) {
        if (this.wellFormed) {
            return new IntelliSheet(this.sheet, new StructuredSheetParser());
        } else {
            return new IntelliSheet(this.sheet, new SemiStructuredSheetBitmapParser());
        }
    }

    private boolean openWithEncoding(final File txtFile, final String encoding) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding))) {
            this.sheet = null;

            if (encoding.equals("UTF-8")) {
                this.processBOM(reader);
            }

            final List<String[]> rows = this.processRows(reader);

            if (checkIfGoodEncoding(rows.get(0))) {
                final String sheetName = Disk.removeExtension(txtFile.getName());
                this.sheet = new CsvSheet(sheetName, rows);
            }

            return this.sheet != null;

        } catch (final IOException x) {
            return false;
        }
    }

    private boolean checkIfGoodEncoding(final String[] row) {
        boolean result = true;
        for (int i = 0; i < row.length; i++) {
            result &= StringUtils.checkIfGoodEncoding(row[i]);
        }
        return result;
    }

    private void processBOM(final BufferedReader reader) throws IOException {
        // skip BOM if present
        reader.mark(1);
        if (reader.read() != StringUtils.BOM_CHAR) {
            reader.reset();
        }
    }

    private List<String[]> processRows(final BufferedReader reader) throws IOException {
        List<String[]> rows = new ArrayList<>();

        boolean firstPass = true;
        for (String textRow; (textRow = reader.readLine()) != null;) {

            if (firstPass) {
                this.separator = this.guessSeparator(textRow);
                firstPass = false;
            }

            final String[] cells = parseOneRow(textRow);

            for (int j = 0; j < cells.length; j++) {
                cells[j] = StringUtils.cleanToken(cells[j]);
            }

            rows.add(cells);
        }
        return rows;
    }

    private String[] parseOneRow(final String data) {
        final ArrayList<String> result = new ArrayList<>();
        String acc = "";
        int state = 0;

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

    private String guessSeparator(final String sample) {
        final String[] separators = { "\t", ",", ";" };
        // find the separator generating the more of columns
        final float[] v = new float[separators.length];
        for (int i = 0; i < separators.length; i++) {
            v[i] = sample.split(separators[i], -1).length;
        }
        return separators[(int) Tensor.of(v).argmax(0).item(0)];
    }

    private boolean wellFormed = true;
    private CsvSheet sheet;
    private String separator;
}
