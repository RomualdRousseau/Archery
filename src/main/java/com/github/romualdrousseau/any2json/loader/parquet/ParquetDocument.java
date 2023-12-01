package com.github.romualdrousseau.any2json.loader.parquet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.parser.sheet.SimpleSheetParser;
import com.github.romualdrousseau.any2json.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.any2json.util.Disk;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;

public class ParquetDocument extends BaseDocument {

    private static final int BATCH_SIZE = 10000;

    public static final List<String> EXTENSIONS = List.of(".parquet");

    private ParquetSheet sheet;

    private DataFrame rows;

    @Override
    public boolean open(final File parquetFile, final String encoding, final String password) {
        this.sheet = null;

        if (EXTENSIONS.stream().filter(x -> parquetFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        try {
            final var path = new org.apache.hadoop.fs.Path(parquetFile.toURI());
            final var config = new Configuration();
            final var inputFile = HadoopInputFile.fromPath(path, config);
            try (
                    final var reader = AvroParquetReader.<GenericRecord>builder(inputFile)
                            .disableCompatibility()
                            .build();
                    final var writer = new DataFrameWriter(BATCH_SIZE)) {

                this.rows = this.processRows(reader, writer);
                if (this.rows.getRowCount() > 0) {
                    final String sheetName = Disk.removeExtension(parquetFile.getName());
                    this.sheet = new ParquetSheet(sheetName, this.rows);
                }

                return this.sheet != null;
            }
        } catch (IOException x) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (this.sheet != null) {
                this.sheet.close();
                this.sheet = null;
            }
            if (this.rows != null) {
                this.rows.close();
                this.rows = null;
            }
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        }
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
        super.autoRecipe(sheet);
        if (this.getHints().contains(Document.Hint.INTELLI_LAYOUT)) {
            DropColumnsWhenFillRatioLessThan.Apply(sheet, 0);
        }
    }

    @Override
    public void updateParsersAndClassifiers() {
        super.updateParsersAndClassifiers();
        this.setSheetParser(new SimpleSheetParser());
    }

    private DataFrame processRows(final ParquetReader<GenericRecord> reader, final DataFrameWriter writer)
            throws IOException {
        var firstPass = true;
        for (GenericRecord record; (record = reader.read()) != null;) {
            if (firstPass) {
                writer.write(Row.of(parseHeader(record)));
                firstPass = false;
            }
            writer.write(Row.of(parseOneRecord(record)));
        }

        return writer.getDataFrame();
    }

    private String[] parseHeader(final GenericRecord record) {
        return record.getSchema().getFields().stream()
                .map(x -> StringUtils.cleanToken(x.name()))
                .toArray(String[]::new);
    }

    private String[] parseOneRecord(final GenericRecord record) {
        return record.getSchema().getFields().stream()
                .map(x -> {
                    final var value = record.get(x.pos());
                    return (value != null) ? StringUtils.cleanToken(value.toString()) : "";
                })
                .toArray(String[]::new);
    }
}
