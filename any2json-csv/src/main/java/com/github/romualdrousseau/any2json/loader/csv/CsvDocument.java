package com.github.romualdrousseau.any2json.loader.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.strings.StringUtils;

public class CsvDocument extends BaseDocument {

    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private CsvSheet sheet;

    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File txtFile, final String encoding, final String password) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;

        if (encoding != null && this.openWithEncoding(txtFile, encoding)) {
            return true;
        } else if (this.openWithEncoding(txtFile, "UTF-8")) {
            return true;
        } else {
            this.close();
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
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        } finally {
            super.close();
        }
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public String getSheetNameAt(final int i) {
        return this.sheet.getName();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheet.getName(), this.sheet.ensureDataLoaded());
    }

    @Override
    public void autoRecipe(final BaseSheet sheet) {
        super.autoRecipe(sheet);
        if (this.getHints().contains(Document.Hint.INTELLI_LAYOUT)) {
            DropColumnsWhenFillRatioLessThan.Apply(sheet, 0);
        }
    }

    private boolean openWithEncoding(final File txtFile, final String encoding) {
        try {
            final var reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));
            if (encoding.startsWith("UTF-")) {
                this.processUtfBOM(reader);
            }
            final var sheetName = Disk.removeExtension(txtFile.getName());
            this.sheet = new CsvSheet(sheetName, reader);
            this.sheet.checkDataEncoding();
            return true;
        } catch (final IOException x) {
            return false;
        }
    }

    private void processUtfBOM(final BufferedReader reader) throws IOException {
        // skip BOM if present
        reader.mark(1);
        if (reader.read() != StringUtils.BOM_CHAR) {
            reader.reset();
        }
    }
}
