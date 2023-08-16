package com.github.romualdrousseau.any2json.loader.excel.xlsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.shuju.util.StringUtils;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;

public class XlsxDocument extends BaseDocument {

    public static List<String> EXTENSIONS = List.of(".xls", ".xlsx");

    @Override
    public boolean open(final File excelFile, final String encoding, final String password) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        if (EXTENSIONS.stream().filter(x -> excelFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        try {
            this.sheets.clear();

            if (!StringUtils.isBlank(password)) {
                POIFSFileSystem poifs = new POIFSFileSystem(excelFile);
                EncryptionInfo info = new EncryptionInfo(poifs);
                Decryptor d = Decryptor.getInstance(info);
                d.verifyPassword(password);
                this.opcPackage = OPCPackage.open(d.getDataStream(poifs));
            } else {
                this.opcPackage = OPCPackage.open(excelFile.getAbsolutePath(), PackageAccess.READ);
            }

            final XSSFReader reader = new XSSFReader(this.opcPackage);
            final SharedStrings sharedStrings = reader.getSharedStringsTable();
            final StylesTable styles = reader.getStylesTable();

            final SheetIterator it = (SheetIterator) reader.getSheetsData();
            while (it.hasNext()) {
                InputStream sheetData = it.next();
                this.sheets.add(new XlsxSheet(it.getSheetName(), sheetData, sharedStrings, styles));
            }

            return this.sheets.size() > 0;

        } catch (IllegalArgumentException | IOException | OpenXML4JException | GeneralSecurityException e) {
            close();
            return false;
        }
    }

    @Override
    public void close() {
        this.sheets.clear();
        if (this.opcPackage != null) {
            try {
                this.opcPackage.close();
                this.opcPackage = null;
            } catch (final IOException ignore) {
            }
        }
    }

    @Override
    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, sheets.get(i).getName(), sheets.get(i).ensureDataLoaded());
    }

    private OPCPackage opcPackage;
    private final ArrayList<XlsxSheet> sheets = new ArrayList<XlsxSheet>();
}
