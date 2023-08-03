package com.github.romualdrousseau.any2json.loader.dbf;

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
import com.github.romualdrousseau.any2json.intelli.parser.SemiStructuredSheetParser;
import com.github.romualdrousseau.any2json.intelli.parser.StructuredSheetParser;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.util.StringUtils;

public class DbfDocument implements Document {

    public static List<String> EXTENSIONS = List.of(".dbf");

    @Override
    public boolean open(final File txtFile, final String encoding, final String password, final boolean wellFormed) {
        this.wellFormed = wellFormed;

        if (EXTENSIONS.stream().filter(x -> txtFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        if (this.openWithEncoding(txtFile, "UTF-8")) {
            return true;
        } else if (encoding != null) {
            return this.openWithEncoding(txtFile, encoding);
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (final IOException ignore) {
            }
            this.reader = null;
        }
        this.sheet = null;
        this.rows = null;
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
            return new IntelliSheet(this.sheet, new SemiStructuredSheetParser());
        }
    }

    private boolean openWithEncoding(final File txtFile, final String encoding) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        try {
            this.sheet = null;
            this.rows = null;

            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));

            if (encoding.equals("UTF-8")) {
                this.processBOM(reader);
            }

            this.processRows(reader);

            if (checkIfGoodEncoding(this.rows.get(0))) {
                final String sheetName = Disk.removeExtension(txtFile.getName());
                this.sheet = new DbfSheet(sheetName, this.rows);
            }

            return this.sheet != null;

        } catch (final IOException x) {
            close();
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
        this.reader.mark(1);
        if (this.reader.read() != StringUtils.BOM_CHAR) {
            this.reader.reset();
        }
    }

    private void processRows(final BufferedReader reader) throws IOException {
        this.rows = new ArrayList<String[]>();

        boolean firstPass = true;
        for (String textRow; (textRow = reader.readLine()) != null;) {

            if (firstPass) {
                this.separator = this.guessSeparator(textRow);
                firstPass = false;
            }

            final String[] tokens = parseOneRow(textRow);

            final String[] cells = new String[tokens.length];
            for (int j = 0; j < tokens.length; j++) {
                cells[j] = StringUtils.cleanToken(tokens[j]);
            }

            this.rows.add(cells);
        }
    }

    private String[] parseOneRow(final String data) {
        final ArrayList<String> result = new ArrayList<String>();
        String acc = "";
        int state = 0;

        for (int i = 0; i < data.length(); i++) {
            final char c = data.charAt(i);

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

        return result.toArray(new String[result.size()]);
    }

    private String guessSeparator(final String sample) {
        final String[] separators = { "\t", ",", ";" };
        // find the separator generating the more of columns
        final float[] v = new float[separators.length];
        for (int i = 0; i < separators.length; i++) {
            v[i] = sample.split(separators[i], -1).length;
        }
        return separators[(int) Tensor.create(v).argmax(0).item(0)];
    }

    private boolean wellFormed = true;
    private DbfSheet sheet;
    private BufferedReader reader;
    private ArrayList<String[]> rows;
    private String separator;
}
