package com.github.romualdrousseau.any2json.loader.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.parser.sheet.SimpleSheetParser;
import com.github.romualdrousseau.any2json.parser.table.SimpleTableParser;
import com.github.romualdrousseau.any2json.util.Disk;

public class PdfDocument extends BaseDocument {

    private static final List<String> EXTENSIONS = List.of(".pdf");

    private PdfSheet sheet;

    @Override
    public boolean open(final File pdfFile, final String encoding, final String password) {
        if (pdfFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;

        if (EXTENSIONS.stream().filter(x -> pdfFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        if (encoding != null && this.openWithEncoding(pdfFile, encoding)) {
            return true;
        } else if (this.openWithEncoding(pdfFile, "ISO-8859-1")) {
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
    }

    @Override
    public void updateParsersAndClassifiers() {
        super.updateParsersAndClassifiers();
        this.setSheetParser(new SimpleSheetParser());
        this.setTableParser(new SimpleTableParser());
    }

    private boolean openWithEncoding(final File pdfFile, final String encoding) {
        try {
            final var reader = PDDocument.load(new FileInputStream(pdfFile));
            final var sheetName = Disk.removeExtension(pdfFile.getName());
            this.sheet = new PdfSheet(sheetName, reader);
            return true;
        } catch (final IOException | UnsupportedCharsetException x) {
            return false;
        }
    }
}
