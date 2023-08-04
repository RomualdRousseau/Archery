package com.github.romualdrousseau.any2json.loader.dbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.parser.sheet.StructuredSheetParser;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.util.StringUtils;
import com.linuxense.javadbf.DBFReader;

public class DbfDocument implements Document {

    public static List<String> EXTENSIONS = List.of(".dbf");

    @Override
    public boolean open(final File dbfFile, final String encoding, final String password, final boolean wellFormed) {

        if (EXTENSIONS.stream().filter(x -> dbfFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        if (this.openWithEncoding(dbfFile, "ISO-8859-1")) {
            return true;
        } else if (encoding != null) {
            return this.openWithEncoding(dbfFile, encoding);
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        this.sheet = null;
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new IntelliSheet(this.sheet, new StructuredSheetParser());
    }

    private boolean openWithEncoding(final File dbfFile, final String encoding) {
        if (dbfFile == null) {
            throw new IllegalArgumentException();
        }

        try (DBFReader reader = new DBFReader(new FileInputStream(dbfFile), Charset.forName(encoding))) {

            final List<String[]> rows = this.processRows(reader);

            final String sheetName = Disk.removeExtension(dbfFile.getName());
            this.sheet = new DbfSheet(sheetName, rows);

            return true;

        } catch (final IOException x) {
            return false;
        }
    }

    private List<String[]> processRows(final DBFReader reader) throws IOException {
        final List<String[]> rows = new ArrayList<String[]>();

        for (Object[] rowObjects; (rowObjects = reader.nextRecord()) != null;) {

            final String[] cells = new String[rowObjects.length];
            for (int j = 0; j < rowObjects.length; j++) {
                cells[j] = StringUtils.cleanToken(rowObjects[j].toString());
            }

            rows.add(cells);
        }
        return rows;
    }

    private DbfSheet sheet;
}
