package com.github.romualdrousseau.archery.loader.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.EnumSet;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.commons.io.FileOps;

public class PdfDocument extends BaseDocument {

    private static final List<String> EXTENSIONS = List.of(".pdf");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_EXTRACT,
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private PdfSheet sheet;

    @Override
    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File pdfFile, final String encoding, final String password, final String sheetName) {
        if (pdfFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;
        if (EXTENSIONS.stream().filter(x -> pdfFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        final var sheetName2 = (sheetName == null) ? FileOps.removeExtension(pdfFile.getName()) : sheetName;
        if (encoding != null && this.openWithEncoding(pdfFile, encoding, sheetName2)) {
            return true;
        } else if (this.openWithEncoding(pdfFile, "ISO-8859-1", sheetName2)) {
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

    private boolean openWithEncoding(final File pdfFile, final String encoding, final String sheetName) {
        try {
            final var reader = PDDocument.load(new FileInputStream(pdfFile));
            this.sheet = new PdfSheet(sheetName, reader);
            return true;
        } catch (final IOException | UnsupportedCharsetException x) {
            return false;
        }
    }
}
