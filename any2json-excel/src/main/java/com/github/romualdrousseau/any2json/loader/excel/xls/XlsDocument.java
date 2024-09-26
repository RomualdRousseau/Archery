package com.github.romualdrousseau.any2json.loader.excel.xls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;
import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;

public class XlsDocument extends BaseDocument {

    private static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xlsm");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_EXTRACT,
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private final ArrayList<XlsSheet> sheets = new ArrayList<XlsSheet>();

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

        if (!StringUtils.isBlank(password)) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
        }
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            this.sheets.clear();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                this.sheets.add(new XlsSheet(workbook.getSheetAt(i)));
            }
            return this.sheets.size() > 0;
        } catch (EncryptedDocumentException | IOException e) {
            this.close();
            return false;
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
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
