package com.github.romualdrousseau.any2json.util;

import java.io.Closeable;
import java.io.IOException;
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
}
