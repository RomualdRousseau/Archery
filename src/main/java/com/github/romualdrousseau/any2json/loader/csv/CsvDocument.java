package com.github.romualdrousseau.any2json.loader.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.parser.sheet.SimpleSheetParser;
import com.github.romualdrousseau.any2json.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;
import com.github.romualdrousseau.shuju.types.Tensor;


public class CsvDocument extends BaseDocument {

    private static final int BATCH_SIZE = 10000;
    private static final String[] SEPARATORS = { "\t", ",", ";" };

    @Override
    public boolean open(final File txtFile, final String encoding, final String password) {
        this.sheet = null;

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
        try {
            if (this.sheet != null) {
                this.sheet.close();
                this.sheet = null;
            }
            if (this.rows != null) {
                this.rows.close();
                this.rows = null;
            }
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        }
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheet.getName(), this.sheet);
    }

    @Override
    public void autoRecipe(final BaseSheet sheet) {
        super.autoRecipe(sheet);
        if (this.getHints().contains(Document.Hint.INTELLI_LAYOUT)) {
            DropColumnsWhenFillRatioLessThan.Apply(sheet, 0);
        }
    }

    @Override
    public void updateParsersAndClassifiers() {
        super.updateParsersAndClassifiers();
        this.setSheetParser(new SimpleSheetParser());
    }

    private boolean openWithEncoding(final File txtFile, final String encoding) {
        if (txtFile == null || encoding == null) {
            throw new IllegalArgumentException();
        }

        try (
            final var reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));
            final var writer = new DataFrameWriter(BATCH_SIZE);
            ) {

            if (encoding.equals("UTF-8")) {
                this.processBOM(reader);
            }

            this.rows = this.processRows(reader, writer);
            if (this.rows.getRowCount() > 0) {
                final String sheetName = Disk.removeExtension(txtFile.getName());
                this.sheet = new CsvSheet(sheetName, this.rows);
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

    private DataFrame processRows(final BufferedReader reader, final DataFrameWriter writer) throws IOException {
        var firstPass = true;
        for (String textRow; (textRow = reader.readLine()) != null;) {

            if (firstPass) {
                this.separator = this.guessSeparator(textRow);
            }

            final String[] cells = parseOneRow(textRow);

            if (firstPass) {
                if (!this.checkIfGoodEncoding(cells)) {
                    throw new IOException("CSV bad encoding");
                }
                firstPass = false;
            }

            for (int j = 0; j < cells.length; j++) {
                cells[j] = StringUtils.cleanToken(cells[j]);
            }

            writer.write(Row.of(cells));
        }

        return writer.getDataFrame();
    }

    private String[] parseOneRow(final String data) {
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

    private String guessSeparator(final String sample) {
        // find the separator generating the more of columns
        final float[] v = new float[SEPARATORS.length];
        for (int i = 0; i < SEPARATORS.length; i++) {
            v[i] = sample.split(SEPARATORS[i], -1).length;
        }
        return SEPARATORS[(int) Tensor.of(v).argmax(0).item(0)];
    }

    private CsvSheet sheet;
    private String separator;
    private DataFrame rows;
}
