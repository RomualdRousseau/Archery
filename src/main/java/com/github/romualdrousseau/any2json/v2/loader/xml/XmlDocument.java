package com.github.romualdrousseau.any2json.v2.loader.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import nl.fountain.xelem.excel.Workbook;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.lex.ExcelReader;

import com.github.romualdrousseau.any2json.v2.Document;
import com.github.romualdrousseau.any2json.v2.Sheet;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlDocument implements Document {
    public boolean open(File excelFile, String encoding) {
        if (openWithEncoding(excelFile, null)) {
            return true;
        } else {
            return openWithEncoding(excelFile, encoding);
        }
    }

    public void close() {
        this.sheets.clear();
        if (this.workbook == null) {
            return;
        }
    }

    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    public Sheet getSheetAt(int i) {
        return this.sheets.get(i);
    }

    private boolean openWithEncoding(File excelFile, String encoding) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        close();

        try {
            ExcelReader reader = new ExcelReader();
            this.workbook = reader.getWorkbook(new InputSource(new FixBadEntityReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(excelFile), encoding)))));

            for (Worksheet sheet : this.workbook.getWorksheets()) {
                this.sheets.add(new XmlSheet(sheet));
            }
        } catch (ParserConfigurationException x) {
            close();
        } catch (SAXException x) {
            close();
        } catch (IOException x) {
            close();
        }

        return this.sheets.size() > 0;
    }

    private Workbook workbook = null;
    private ArrayList<XmlSheet> sheets = new ArrayList<XmlSheet>();
}
