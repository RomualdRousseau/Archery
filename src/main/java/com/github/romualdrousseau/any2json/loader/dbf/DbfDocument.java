package com.github.romualdrousseau.any2json.loader.dbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.parser.sheet.StructuredSheetParser;
import com.github.romualdrousseau.any2json.parser.table.SimpleTableParser;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.strings.StringUtils;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

public class DbfDocument extends BaseDocument {

    public static final List<String> EXTENSIONS = List.of(".dbf");
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public boolean open(final File dbfFile, final String encoding, final String password) {

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
        return new BaseSheet(this, this.sheet.getName(), this.sheet);
    }

    @Override
    public void autoRecipe(final BaseSheet sheet) {
    }

    @Override
    public void updateParsersAndClassifiers() {
        super.updateParsersAndClassifiers();
        this.setSheetParser(new StructuredSheetParser());
        this.setTableParser(new SimpleTableParser());
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

        final int numberOfFields = reader.getFieldCount();
        final String[] headers = new String[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            DBFField field = reader.getField(i);
            headers[i] = StringUtils.cleanToken(field.getName());
        }
        rows.add(headers);

        for (Object[] rowObjects; (rowObjects = reader.nextRecord()) != null;) {
            final String[] cells = new String[rowObjects.length];
            for (int j = 0; j < rowObjects.length; j++) {
                cells[j] = StringUtils.cleanToken(this.convertToString(rowObjects[j]));
            }
            rows.add(cells);
        }

        return rows;
    }

    private String convertToString(Object v) {
        if (v instanceof BigDecimal) {
            try {
                return String.valueOf(((BigDecimal) v).longValueExact());
            } catch(ArithmeticException x) {
                return v.toString();
            }
        } else if (v instanceof Date) {
            return DbfDocument.dateFormatter.format((Date) v);
        } else {
            return v.toString();
        }
    }

    private DbfSheet sheet;
}
