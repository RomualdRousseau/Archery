package com.github.romualdrousseau.any2json.loader.dbf;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

class DbfSheet extends PatcheableSheetStore implements Closeable {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final int BATCH_SIZE = 10000;

    private final String name;
    private DBFReader reader;
    private DataFrame rows;

    public DbfSheet(final String name, final DBFReader reader) {
        this.name = name;
        this.reader = reader;
        this.rows = null;
    }

    @Override
    public void close() throws IOException {
        if (this.rows != null) {
            this.rows.close();
        }
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        return this.rows.getColumnCount(rowIndex) - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.getRowCount() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return true;
        } else {
            return this.getCellAt(colIndex, rowIndex) != null;
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return patchCell;
        } else {
            return StringUtils.cleanToken(this.getCellAt(colIndex, rowIndex));
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
    }

    public DbfSheet ensureDataLoaded() {
        if (this.rows != null) {
            return this;
        }
        try (final var writer = new DataFrameWriter(BATCH_SIZE);) {
            this.rows = this.processRows(this.reader, writer);
            this.reader.close();
            this.reader = null;
            return this;
        } catch (final IOException x) {
            return this;
        }
    }

    private DataFrame processRows(final DBFReader reader, final DataFrameWriter writer) throws IOException {
        final List<String[]> rows = new ArrayList<String[]>();

        final int numberOfFields = reader.getFieldCount();
        final String[] headers = new String[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            final DBFField field = reader.getField(i);
            headers[i] = StringUtils.cleanToken(field.getName());
        }
        rows.add(headers);

        for (Object[] rowObjects; (rowObjects = reader.nextRecord()) != null;) {
            final String[] cells = new String[rowObjects.length];
            for (int j = 0; j < rowObjects.length; j++) {
                cells[j] = StringUtils.cleanToken(this.convertToString(rowObjects[j]));
            }
            writer.write(Row.of(cells));
        }

        return writer.getDataFrame();
    }

    private String convertToString(final Object v) {
        if (v instanceof BigDecimal) {
            try {
                return String.valueOf(((BigDecimal) v).longValueExact());
            } catch (final ArithmeticException x) {
                return v.toString();
            }
        } else if (v instanceof Date) {
            return DbfSheet.dateFormatter.format((Date) v);
        } else {
            return v.toString();
        }
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
        if (rowIndex >= this.rows.getRowCount()) {
            return null;
        }

        final Row row = this.rows.getRow(rowIndex);

        if (colIndex >= row.size()) {
            return null;
        }

        return row.get(colIndex);
    }
}
