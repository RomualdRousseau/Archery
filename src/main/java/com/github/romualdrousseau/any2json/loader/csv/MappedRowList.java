package com.github.romualdrousseau.any2json.loader.csv;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;

public class MappedRowList<T> implements Closeable {

    private final int batchSize;
    private final Path storePath;
    private final List<BatchOfRows> batches;
    private final int length;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedByteBuffer;

    private List<T> currentBatch = null;
    private int currentBatchIdx = -1;
    private boolean isClosed = false;

    public MappedRowList(final int batchSize, final Path storePath, final List<BatchOfRows> batches, final int length) {
        try {
            this.batchSize = batchSize;
            this.storePath = storePath;
            this.batches = batches;
            this.length = length;
            this.fileChannel = (FileChannel) Files.newByteChannel(this.storePath, EnumSet.of(StandardOpenOption.READ));
            this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, this.storePath.toFile().length());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed) {
            return;
        }

        this.fileChannel.close();
        this.isClosed = true;

        Files.deleteIfExists(this.storePath);
    }

    public int length() {
        return this.length;
    }

    public T get(final int n) {
        try {
            final int idx = n / batchSize;
            if (this.currentBatchIdx != idx) {
                this.currentBatch = this.loadOneBatch(batches.get(idx));
                this.currentBatchIdx = idx;
            }
            return this.currentBatch.get(n % batchSize);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<T> loadOneBatch(final BatchOfRows batch) throws ClassNotFoundException, IOException {
        final byte[] bytes = new byte[batch.length()];
        this.mappedByteBuffer.position(batch.position());
        this.mappedByteBuffer.get(bytes);
        return this.deserialize(bytes);
    }

    @SuppressWarnings("unchecked")
    private List<T> deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream o = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (List<T>) o.readObject();
        }
    }
}
