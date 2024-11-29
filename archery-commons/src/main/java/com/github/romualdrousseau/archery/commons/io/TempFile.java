package com.github.romualdrousseau.archery.commons.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempFile implements Closeable {

    private final Path path;

    public TempFile(String prefix, String suffix) throws IOException {
        this.path = Files.createTempFile(prefix, suffix);
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(this.path);
    }

    public static File getTempDir(final String folder) {
        try {
            final var tempDir = Files.createTempDirectory(folder).toFile();
            tempDir.deleteOnExit();
            return tempDir;
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static File getTempFile() {
        try {
            final var tempFile = File.createTempFile("deapsea-file-", ".tmp");
            tempFile.deleteOnExit();
            return tempFile;
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static File getTempFile(final String folder) {
        try {
            final var tempFile = File.createTempFile("deapsea-file-", ".tmp", TempFile.getTempDir(folder));
            tempFile.deleteOnExit();
            return tempFile;
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static File getTempFile(final String folder, final String fileName) {
        final var tempFile = new File(TempFile.getTempDir("imports-"), fileName);
        tempFile.deleteOnExit();
        return tempFile;
    }
}
