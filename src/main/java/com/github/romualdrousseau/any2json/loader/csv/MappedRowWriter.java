package com.github.romualdrousseau.any2json.loader.csv;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MappedRowWriter<T> implements Closeable {

    private final int batchSize;
    private final Path storePath;
    private final List<RowBatch> batches = new ArrayList<>();
    private final FileOutputStream fileOutputStream;
    private final List<T> currentBatch;

    private int currPosition = 0;
    private int length = 0;

    public MappedRowWriter(final int batchSize) throws IOException {
        this.batchSize = batchSize;
        this.storePath = Files.createTempFile("any2json-csv-", ".tmp");
        this.fileOutputStream = new FileOutputStream(this.storePath.toFile());
        this.currentBatch = new ArrayList<>();
    }

    @Override
    public void close() throws IOException {
        this.flush();
        this.fileOutputStream.close();
    }

    public int length() {
        return this.length;
    }

    public MappedRowList<T> getMappedList() {
        return new MappedRowList<T>(this.batchSize, this.storePath, this.batches, this.length);
    }

    public void write(final T data) throws IOException {
        this.currentBatch.add(data);
        this.length++;
        if (this.currentBatch.size() >= this.batchSize) {
            this.flush();
        }
    }

    private void flush() throws IOException {
        final byte[] bytes = this.serialize(this.currentBatch);
        this.batches.add(RowBatch.of(currPosition, bytes.length));
        this.fileOutputStream.write(bytes);
        this.currentBatch.clear();
        this.currPosition += bytes.length;
    }

    private byte[] serialize(final Object o) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
    }
}
