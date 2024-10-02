package com.github.romualdrousseau.archery.loader.parquet;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.archery.util.Disk;

public class ParquetDocument extends BaseDocument {

    private static final List<String> EXTENSIONS = List.of(".parquet");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(
            Document.Hint.INTELLI_LAYOUT,
            Document.Hint.INTELLI_TAG);

    private ParquetSheet sheet;

    @Override
    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File parquetFile, final String encoding, final String password, final String sheetName) {
        if (parquetFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;

        if (EXTENSIONS.stream().filter(x -> parquetFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        try {
            final var path = new org.apache.hadoop.fs.Path(parquetFile.toURI());
            final var config = new Configuration();
            final var file = HadoopInputFile.fromPath(path, config);
            final var reader = AvroParquetReader.<GenericRecord>builder(file).disableCompatibility().build();
            final var sheetName2 = (sheetName == null) ? Disk.removeExtension(parquetFile.getName()) : sheetName;
            this.sheet = new ParquetSheet(sheetName2, reader);
            return true;
        } catch (IOException x) {
            this.close();
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
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        } finally {
            super.close();
        }
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public String getSheetNameAt(final int i) {
        return this.sheet.getName();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheet.getName(), this.sheet.ensureDataLoaded());
    }

    @Override
    public void autoRecipe(final BaseSheet sheet) {
        super.autoRecipe(sheet);
        if (this.getHints().contains(Document.Hint.INTELLI_LAYOUT)) {
            DropColumnsWhenFillRatioLessThan.Apply(sheet, 0);
        }
    }
}
