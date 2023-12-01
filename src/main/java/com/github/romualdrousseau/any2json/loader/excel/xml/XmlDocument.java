package com.github.romualdrousseau.any2json.loader.excel.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    public static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xlsm", ".xml");

    private Workbook workbook = null;

    private final ArrayList<XmlSheet> sheets = new ArrayList<XmlSheet>();

    @Override
    public boolean open(final File excelFile, final String encoding, final String password) {
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
    public void updateParsersAndClassifiers() {
        if (this.getHints().contains(Document.Hint.INTELLI_TAG)) {
            this.getHints().add(Document.Hint.INTELLI_LAYOUT);
        }
        super.updateParsersAndClassifiers();
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
