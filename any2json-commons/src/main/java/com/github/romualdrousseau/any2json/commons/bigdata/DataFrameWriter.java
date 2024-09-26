package com.github.romualdrousseau.any2json.commons.bigdata;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class DataFrameWriter implements Closeable {

    private final ChunkSerializer serializer = ChunkSerializerFactory.newInstance();

    private final Chunk chunk;
    private final Path storePath;
    private final FileChannel fileChannel;

    private int columnCount;
    private int rowCount;
    private boolean isClosed;

    public DataFrameWriter(final int chunkSize) throws IOException {
        this(chunkSize, 0, null);
    }

    public DataFrameWriter(final int chunkSize, final int columnCount) throws IOException {
        this(chunkSize, columnCount, null);
    }

    public DataFrameWriter(final int batchSize, final Path path) throws IOException {
        this(batchSize, 0, path);
    }

    public DataFrameWriter(final int chunkSize, final int columnCount, final Path path) throws IOException {
        this.chunk = new Chunk(chunkSize);
        this.storePath = (path == null) ? Files.createTempFile(null, null) : Files.createTempFile(path, null, null);
        this.storePath.toFile().deleteOnExit();
        this.fileChannel = (FileChannel) Files.newByteChannel(this.storePath,
                EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
        this.columnCount = columnCount;
        this.rowCount = 0;
        this.isClosed = false;
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed) {
            return;
        }

        if ((this.rowCount % this.chunk.getBatchSize()) > 0) {
            this.flushCurrentChunk();
        }

        this.fileChannel.close();
        this.isClosed = true;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public DataFrame getDataFrame() throws IOException {
        this.close();
        return new DataFrame(this.chunk, this.storePath, this.rowCount, this.columnCount);
    }

    public void write(final Row data) throws IOException {
        this.chunk.setRow(this.rowCount % this.chunk.getBatchSize(), data);
        this.columnCount = Math.max(this.columnCount, data.size());
        this.rowCount++;
        if ((this.rowCount % this.chunk.getBatchSize()) == 0) {
            this.flushCurrentChunk();
        }
    }

    private void flushCurrentChunk() throws IOException {
        final var bytes = serializer.serialize(this.chunk.getRows());
        this.chunk.getBatches().add(ChunkMetaData.of(this.fileChannel.position(), bytes.length));
        this.fileChannel.write(ByteBuffer.wrap(bytes));
    }
}
