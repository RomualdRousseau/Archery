package com.github.romualdrousseau.archery.commons.bigdata;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFrame implements Closeable, Iterable<Row> {
    private final Logger logger = LoggerFactory.getLogger(DataFrame.class);

    private final ChunkSerializer serializer = ChunkSerializerFactory.newInstance();

    private final Chunk chunk;
    private final Path storePath;
    private final int rowCount;
    private final int columnCount;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedBuffer;

    private int currentChunkIdx;
    private boolean isClosed;

    public DataFrame(final Chunk chunk, final Path storePath, final int rowCount, final int columnCount)
            throws IOException {
        this.chunk = chunk;
        this.storePath = storePath;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.fileChannel = (FileChannel) Files.newByteChannel(this.storePath,
                EnumSet.of(StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE));
        if (this.fileChannel.size() <= Integer.MAX_VALUE) {
            this.mappedBuffer = this.fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, this.fileChannel.size());
        } else {
            this.mappedBuffer = null;
        }

        this.currentChunkIdx = -1;
        this.isClosed = false;

        this.logger.info("DataFrame initialized with Mapped Buffer: {}", this.isMappedBuffer());
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed) {
            return;
        }
        this.fileChannel.close();
        this.isClosed = true;
    }

    public DataView view(final int rowStart, final int columnStart, final int rowCount, final int columnCount) {
        Objects.checkFromToIndex(rowStart, rowStart + rowCount - 1, this.rowCount);
        Objects.checkFromToIndex(columnStart, columnStart + columnCount - 1, this.columnCount);
        return new DataView(this, rowStart, columnStart, rowCount, columnCount);
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public int getColumnCount(final int row) {
        Objects.checkIndex(row, this.rowCount);
        final var r = this.getRow(row);
        return r.size();
    }

    public Row getRow(final int row) {
        Objects.checkIndex(row, this.rowCount);
        final int idx = row / this.chunk.getBatchSize();
        if (this.currentChunkIdx != idx) {
            this.chunk.setRows(this.loadOneBatch(this.chunk.getBatches().get(idx)));
            this.currentChunkIdx = idx;
        }
        return this.chunk.getRow(row % this.chunk.getBatchSize());
    }

    public String getCell(final int row, final int column) {
        Objects.checkIndex(row, this.rowCount);
        Objects.checkIndex(column, this.columnCount);
        return this.getRow(row).get(column);
    }

    @Override
    public Iterator<Row> iterator() {
        return new DataFrameIterator(this);
    }

    private Row[] loadOneBatch(final ChunkMetaData batch) {
        final long startTime = System.currentTimeMillis();
        try {

            if (this.isMappedBuffer()) {
                final var bytes = new byte[batch.length()];
                this.mappedBuffer.position((int) batch.position());
                this.mappedBuffer.get(bytes);
                return serializer.deserialize(bytes);
            } else {
                final var bytes = ByteBuffer.allocate(batch.length());
                this.fileChannel.position(batch.position());
                this.fileChannel.read(bytes);
                return serializer.deserialize(bytes.array());
            }
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        } finally {
            final var stopTime = System.currentTimeMillis();
            final var executionTimeInMS = (int) (stopTime - startTime);
            this.logger.debug("Load a chunk in memory offset: {}, lenght: {}. Took {}ms", batch.position(), batch.length(), executionTimeInMS);
        }
    }

    private boolean isMappedBuffer() {
        return this.mappedBuffer != null;
    }
}
