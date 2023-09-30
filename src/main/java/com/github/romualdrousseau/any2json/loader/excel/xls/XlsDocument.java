package com.github.romualdrousseau.any2json.loader.excel.xls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.shuju.strings.StringUtils;
import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;

public class XlsDocument extends BaseDocument {

    public static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xlsm");

    @Override
    public boolean open(final File excelFile, final String encoding, final String password) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        if (!StringUtils.isBlank(password)) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
        }

        if (EXTENSIONS.stream().filter(x -> excelFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
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
            this.close();
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

    @Override
    public void close() {
        if (this.sheets != null) {
            this.sheets.clear();
        }
    }

    @Override
    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheets.get(i).getName(), this.sheets.get(i));
    }

    @Override
    public void updateParsersAndClassifiers() {
        if(this.getHints().contains(Document.Hint.INTELLI_TAG)) {
            this.getHints().add(Document.Hint.INTELLI_LAYOUT);
        }
        super.updateParsersAndClassifiers();
    }

    private final ArrayList<XlsSheet> sheets = new ArrayList<XlsSheet>();
}
