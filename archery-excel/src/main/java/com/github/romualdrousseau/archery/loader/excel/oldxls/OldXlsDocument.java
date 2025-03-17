package com.github.romualdrousseau.archery.loader.excel.oldxls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;

public class OldXlsDocument extends BaseDocument {

    private static List<String> EXTENSIONS = List.of(".xls");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_EXTRACT,
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private final ArrayList<OldXlsSheet> sheets = new ArrayList<OldXlsSheet>();

    @Override
    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File excelFile, final String encoding, final String password, final String sheetName) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheets.clear();

        if (EXTENSIONS.stream().filter(x -> excelFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        final var settings = new WorkbookSettings();
        settings.setEncoding(encoding);

        try {
            final var workbook = Workbook.getWorkbook(excelFile, settings);
            this.sheets.clear();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                this.sheets.add(new OldXlsSheet(workbook.getSheet(i)));
            }
            return this.sheets.size() > 0;
        } catch (BiffException | IndexOutOfBoundsException | IOException e) {
            this.close();
            return false;
        } finally {
        }
    }

    @Override
    public void close() {
        this.sheets.clear();
        super.close();
    }

    @Override
    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    @Override
    public String getSheetNameAt(final int i) {
        return this.sheets.get(i).getName();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheets.get(i).getName(), this.sheets.get(i));
    }

    @Override
    public Document setHints(final EnumSet<Hint> hints) {
        if (hints.contains(Document.Hint.INTELLI_TAG)) {
            hints.add(Document.Hint.INTELLI_LAYOUT);
        }
        if (hints.contains(Document.Hint.INTELLI_LAYOUT)) {
            hints.add(Document.Hint.INTELLI_EXTRACT);
        }
        return super.setHints(hints);
    }
}
