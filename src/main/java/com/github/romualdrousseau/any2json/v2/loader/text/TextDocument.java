package com.github.romualdrousseau.any2json.v2.loader.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.Document;
import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class TextDocument implements Document {

    @Override
    public boolean open(File txtFile, String encoding) {
        if (openWithEncoding(txtFile, "UTF-8")) {
            return true;
        } else if(encoding != null) {
            return openWithEncoding(txtFile, encoding);
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        if(this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                e.printStackTrace();
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
    public Sheet getSheetAt(int i) {
        return this.sheet;
    }

    private boolean openWithEncoding(File txtFile, String encoding) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        try {
            this.sheet = null;
            this.rows = null;

            this.reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(txtFile), encoding));

            if (encoding.equals("UTF-8")) {
                this.processBOM(reader);
            }

            this.processRows(reader);

            if(checkIfGoodEncoding(this.rows.get(0))) {
                String sheetName = StringUtility.removeExtension(txtFile.getName());
                this.sheet = new TextSheet(sheetName, this.rows);
            }

            return this.sheet != null;

        } catch (IOException x) {
            close();
            return false;
        }
    }

    private boolean checkIfGoodEncoding(String[] row) {
        boolean result = true;
        for (int i = 0; i < row.length; i++) {
            result &= StringUtility.checkIfGoodEncoding(row[i]);
        }
        return result;
    }

    private void processBOM(BufferedReader reader) throws IOException {
        // skip BOM if present
        this.reader.mark(1);
        if (this.reader.read() != StringUtility.BOM_CHAR) {
            this.reader.reset();
        }
    }

    private void processRows(BufferedReader reader) throws IOException {
        this.rows = new ArrayList<String[]>();

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

            this.rows.add(cells);
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
        return separators[new Tensor1D(v).argmax()];
    }

    private TextSheet sheet;
    private BufferedReader reader;
    private ArrayList<String[]> rows;
    private String separator;
}
