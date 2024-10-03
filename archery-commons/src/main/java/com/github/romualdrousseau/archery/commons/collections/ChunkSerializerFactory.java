package com.github.romualdrousseau.archery.commons.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.romualdrousseau.archery.commons.collections.serializer.ChunkSerializerFury;
import com.github.romualdrousseau.archery.commons.collections.serializer.ChunkSerializerJava;

public class ChunkSerializerFactory {

    public enum SerializerType {
        DEFAULT, // DEFAULT IS FURY
        JAVA,
        FURY
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkSerializerFactory.class);

    private static final ThreadLocal<ChunkSerializer> CONTEXT = new ThreadLocal<>();

    public static ChunkSerializer newInstance() {
        return ChunkSerializerFactory.newInstance(SerializerType.DEFAULT);
    }

    public static ChunkSerializer newInstance(final SerializerType type) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new ChunkSerializerFactory(type).createSerializerInstance());
        }
        return CONTEXT.get();
    }

    private final SerializerType type;

    private ChunkSerializerFactory(final SerializerType type) {
        try {
            if (type.equals(SerializerType.DEFAULT)) {
                final var prop = new Properties();
                prop.load(this.openDefaultPropertiesInputStream());
                final var typeVal = prop.getProperty("serializer");
                if (typeVal != null) {
                    this.type = Enum.valueOf(SerializerType.class, typeVal);
                } else {
                    this.type = type;
                }
            } else {
                this.type = type;
            }
            LOGGER.info("ChunkSerializerFactor set to {}", this.type);
        } catch (final IOException x) {
            LOGGER.error("Error during ChunkSerializerFactor initialization: {}", x.getMessage());
            throw new UncheckedIOException(x);
        }
    }

    private ChunkSerializer createSerializerInstance() {
        switch (this.type) {
            case JAVA:
                return new ChunkSerializerJava();
            case FURY:
                return new ChunkSerializerFury();
            default:
                return new ChunkSerializerFury();
        }
    }

    private InputStream openDefaultPropertiesInputStream() throws IOException {
        return this.openPropertiesInputStream("chunk-serializer.properties")
                .or(() -> this.openPropertiesInputStream("batch-serializer.properties"))
                .orElseGet(InputStream::nullInputStream);
    }

    private Optional<InputStream> openPropertiesInputStream(final String fileName) {
        final var userDir = System.getProperty("user.dir");
        return this.getPathIfExists(Path.of(userDir, fileName))
                .or(() -> this.getPathIfExists(Path.of(userDir, "classes", fileName)))
                .flatMap(this::pathToStream)
                .or(() -> this.resolveResourceAsStream(fileName));
    }

    private Optional<InputStream> pathToStream(final Path x) {
        try {
            return Optional.of(Files.newInputStream(x));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    private Optional<InputStream> resolveResourceAsStream(final String resourceName) {
        final InputStream resource = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resource == null) {
            LOGGER.debug("module: {} not found", resourceName);
            return Optional.empty();
        }
        LOGGER.debug("module: {} found at {}", resourceName, this.getClass().getClassLoader().getResource(resourceName));
        return Optional.of(resource);
    }

    private Optional<Path> getPathIfExists(final Path path) {
        if (!path.toFile().exists()) {
            LOGGER.debug("module: {} not found at {}", path.getFileName(), path);
            return Optional.empty();
        }
        LOGGER.debug("module: {} found at {}", path.getFileName(), path);
        return Optional.of(path);
    }
}
