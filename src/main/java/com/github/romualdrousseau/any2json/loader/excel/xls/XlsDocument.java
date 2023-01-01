package com.github.romualdrousseau.any2json.loader.excel.xls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.parser.SemiStructuredSheetParser;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class XlsDocument implements Document {

    @Override
    public boolean open(final File excelFile, final String encoding, final String password) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        if (!StringUtility.isEmpty(password)) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
        }

        Workbook workbook = null;
        try {
            this.sheets.clear();

            workbook = WorkbookFactory.create(excelFile);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                this.sheets.add(new XlsSheet(workbook.getSheetAt(i)));
            }

            return this.sheets.size() > 0;

        } catch (EncryptedDocumentException | IOException e) {
            close();
            return false;

        } finally {
            if(workbook != null) {
                try {
                    workbook.close();
                } catch (final IOException ignore) {
                }
            }
            workbook = null;
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    public void close() {
        this.sheets.clear();
    }

    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    public Sheet getSheetAt(final int i) {
        return new IntelliSheet(this.sheets.get(i), new SemiStructuredSheetParser());
    }

    private final ArrayList<XlsSheet> sheets = new ArrayList<XlsSheet>();
}
