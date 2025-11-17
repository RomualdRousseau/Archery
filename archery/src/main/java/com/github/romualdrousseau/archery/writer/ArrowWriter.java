package com.github.romualdrousseau.archery.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.commons.preprocessing.tokenizer.ShingleTokenizer;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class ArrowWriter {

    private static final int BATCH_SIZE = 65536;

    private final BaseTable table;

    public ArrowWriter(final BaseTable table) {
        this.table = table;
    }

    public void write(final String outputFilePath) throws IOException {
        try (
                final var allocator = new RootAllocator();
                final var out = new FileOutputStream(outputFilePath)) {

            // 1) Build Arrow schema from your framework schema

            final var arrowSchema = this.buildArrowSchema(table.headers());

            // 2) Create vectors for each field

            final var fields = arrowSchema.getFields();
            final var vectors = new ArrayList<FieldVector>();
            for (final var field : fields) {
                final var vector = field.createVector(allocator);
                vector.setInitialCapacity(BATCH_SIZE);
                vector.allocateNew();
                vectors.add(vector);
            }

            try (
                    final var root = new VectorSchemaRoot(fields, vectors, 0);
                    final var writer = new ArrowStreamWriter(root, null, out)) {

                writer.start();

                // 3) Stream rows from your framework and write in batches

                int currentIndex = 0;
                final var iterator = table.rows().iterator();
                while (iterator.hasNext()) {
                    final var row = iterator.next();

                    // Set values in each vector
                    for (int colIndex = 0; colIndex < fields.size(); colIndex++) {
                        final var vector = vectors.get(colIndex);
                        final var value = row.getCellAt(colIndex).getValue();
                        this.setValue(vector, currentIndex, value);
                    }

                    currentIndex++;

                    // If batch full, flush to writer
                    if (currentIndex == BATCH_SIZE) {
                        this.flushBatch(root, vectors, currentIndex, writer);
                        currentIndex = 0;
                    }
                }

                // flush remaining rows (if not multiple of BATCH_SIZE)
                if (currentIndex > 0) {
                    this.flushBatch(root, vectors, currentIndex, writer);
                }

                writer.end();

            } finally {
                for (final var v : vectors) {
                    v.close();
                }
            }
        }
    }

    private Schema buildArrowSchema(final Iterable<Header> headers) {
        final var arrowFields = new ArrayList<Field>();

        final var tagTokenizer = this.table.getSheet().getDocument().getTagClassifier().getTagTokenizer();
        ((ShingleTokenizer) tagTokenizer).disableLemmatization();

        for (final var header : headers) {
            final var name = header.hasTag() ? header.getTag().getValue() : header.getName();
            final var field = new Field(
                    StringUtils.toSnake(name, tagTokenizer),
                    FieldType.nullable(ArrowType.Utf8.INSTANCE),
                    null);
            arrowFields.add(field);
        }

        return new Schema(arrowFields);
    }

    private void setValue(final FieldVector vector, final int index, final String value) {
        if (value == null) {
            vector.setNull(index);
            return;
        }
        final var bytes = value.toString().getBytes(StandardCharsets.UTF_8);
        ((VarCharVector) vector).setSafe(index, bytes);
    }

    private void flushBatch(final VectorSchemaRoot root, final List<FieldVector> vectors, final int rowCount,
            final ArrowStreamWriter writer) throws IOException {

        root.setRowCount(rowCount);

        for (final FieldVector v : vectors) {
            v.setValueCount(rowCount);
        }

        writer.writeBatch();

        // Reuse vectors for the next batch: reset but keep allocated memory
        for (final FieldVector v : vectors) {
            v.reset();
            // v.setInitialCapacity(BATCH_SIZE);
        }
    }
}
