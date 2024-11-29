package com.github.romualdrousseau.archery.loader.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.archery.commons.io.FileOps;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class CsvDocument extends BaseDocument {

    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private CsvSheet sheet;

    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File txtFile, final String encoding, final String password, final String sheetName) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;

        final var sheetName2 = (sheetName == null) ? FileOps.removeExtension(txtFile.getName()) : sheetName;
        if (encoding != null && this.openWithEncoding(txtFile, encoding, sheetName2)) {
            return true;
        } else if (this.openWithEncoding(txtFile, "UTF-8", sheetName2)) {
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

    private boolean openWithEncoding(final File txtFile, final String encoding, final String sheetName) {
        try {
            final var reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding));
            if (encoding.startsWith("UTF-")) {
                this.processUtfBOM(reader);
            }
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
