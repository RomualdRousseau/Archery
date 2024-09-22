package com.github.romualdrousseau.any2json.loader.excel.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.xml.sax.InputSource;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;

import nl.fountain.xelem.excel.Workbook;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.lex.ExcelReader;

public class XmlDocument extends BaseDocument {

    private static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xlsm", ".xml");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_EXTRACT,
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private final ArrayList<XmlSheet> sheets = new ArrayList<XmlSheet>();

    private Workbook workbook = null;

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

        try {
            if (this.openWithEncoding(excelFile, "UTF-8")) {
                return true;
            } else if (encoding != null) {
                return this.openWithEncoding(excelFile, encoding);
            } else {
                return false;
            }
        } catch (Exception e) {
            this.close();
            return false;
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

    private boolean openWithEncoding(final File excelFile, final String encoding) throws Exception {
        final ExcelReader reader = new ExcelReader();
        this.workbook = reader.getWorkbook(new InputSource(new FixBadEntityReader(
                new BufferedReader(new InputStreamReader(new FileInputStream(excelFile), encoding)))));

        for (final Worksheet sheet : this.workbook.getWorksheets()) {
            this.sheets.add(new XmlSheet(sheet));
        }
        return this.sheets.size() > 0;
    }
}
