package com.github.romualdrousseau.archery.loader.excel.xlsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;

public class XlsxDocument extends BaseDocument {

    private static List<String> EXTENSIONS = List.of(".xls", ".xlsx", ".xlsm");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_EXTRACT,
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private final ArrayList<XlsxSheet> sheets = new ArrayList<XlsxSheet>();

    private OPCPackage opcPackage;

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
            if (!StringUtils.isBlank(password)) {
                final var poifs = new POIFSFileSystem(excelFile);
                final var info = new EncryptionInfo(poifs);
                final var decrypt = Decryptor.getInstance(info);
                decrypt.verifyPassword(password);
                this.opcPackage = OPCPackage.open(decrypt.getDataStream(poifs));
            } else {
                this.opcPackage = OPCPackage.open(excelFile.getAbsolutePath(), PackageAccess.READ);
            }

            final var reader = new XSSFReader(this.opcPackage);
            final var sharedStrings = reader.getSharedStringsTable();
            final var styles = reader.getStylesTable();

            final var it = (SheetIterator) reader.getSheetsData();
            while (it.hasNext()) {
                final InputStream sheetData = it.next();
                this.sheets.add(new XlsxSheet(it.getSheetName(), sheetData, sharedStrings, styles));
            }

            return this.sheets.size() > 0;

        } catch (IllegalArgumentException | IOException | OpenXML4JException | GeneralSecurityException e) {
            this.close();
            return false;
        }
    }

    @Override
    public void close() {
        this.sheets.forEach(XlsxSheet::close);
        this.sheets.clear();
        if (this.opcPackage != null) {
            this.opcPackage.revert();
            this.opcPackage = null;
        }
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
        return new BaseSheet(this, this.sheets.get(i).getName(), this.sheets.get(i).ensureDataLoaded());
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
