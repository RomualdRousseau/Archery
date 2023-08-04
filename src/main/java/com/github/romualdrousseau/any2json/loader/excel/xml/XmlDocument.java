package com.github.romualdrousseau.any2json.loader.excel.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.parser.sheet.SemiStructuredSheetBitmapParser;

import nl.fountain.xelem.excel.Workbook;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.lex.ExcelReader;

public class XmlDocument implements Document {

    public static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xml");

    @Override
    public boolean open(final File excelFile, final String encoding, final String password, final boolean wellFormed) {

        if (EXTENSIONS.stream().filter(x -> excelFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        if (this.openWithEncoding(excelFile, "UTF-8")) {
            return true;
        } else if (encoding != null) {
            return this.openWithEncoding(excelFile, encoding);
        } else {
            return false;
        }
    }

    public void close() {
        this.sheets.clear();
    }

    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    public Sheet getSheetAt(final int i) {
        return new IntelliSheet(this.sheets.get(i), new SemiStructuredSheetBitmapParser());
    }

    private boolean openWithEncoding(final File excelFile, final String encoding) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        try {
            this.sheets.clear();

            final ExcelReader reader = new ExcelReader();
            this.workbook = reader.getWorkbook(new InputSource(new FixBadEntityReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(excelFile), encoding)))));

            for (final Worksheet sheet : this.workbook.getWorksheets()) {
                this.sheets.add(new XmlSheet(sheet));
            }

            return this.sheets.size() > 0;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            close();
            return false;
        }
    }

    private Workbook workbook = null;
    private final ArrayList<XmlSheet> sheets = new ArrayList<XmlSheet>();
}
