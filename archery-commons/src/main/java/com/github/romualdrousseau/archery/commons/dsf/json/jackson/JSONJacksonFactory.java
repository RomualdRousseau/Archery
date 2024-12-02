package com.github.romualdrousseau.archery.commons.dsf.json.jackson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFFactory;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;
import com.github.romualdrousseau.archery.commons.io.FileOps;

public class JSONJacksonFactory implements DSFFactory {

    private static final ThreadLocal<ObjectMapper> OBJECT_MAPPER = new ThreadLocal<>() {
        @Override
        protected ObjectMapper initialValue() {
            final var mapper = new ObjectMapper();

            final var prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            mapper.setDefaultPrettyPrinter(prettyPrinter);

            final var streamReadConstraints = StreamReadConstraints
                .builder()
                .maxStringLength(Integer.MAX_VALUE)
                .build();
            mapper.getFactory().setStreamReadConstraints(streamReadConstraints);

            return mapper;
        }
    };

    private final ObjectMapper mapper = OBJECT_MAPPER.get();

    public DSFArray newArray() {
        return new JSONJacksonArray(this.mapper, this.mapper.createArrayNode());
    }

    public DSFArray parseArray(final String data) {
        try {
            return new JSONJacksonArray(this.mapper, this.mapper.readTree(data));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFArray parseArray(final Object object) {
        return new JSONJacksonArray(this.mapper, (JsonNode) object);
    }

    public DSFArray loadArray(final Path filePath) {
        try (final var reader = FileOps.createBufferedReaderUtfBOM(filePath)) {
            return new JSONJacksonArray(this.mapper, this.mapper.readTree(reader));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveArray(final DSFArray a, final Path filePath, final boolean pretty) {
        try {
            if (pretty) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), ((JSONJacksonArray) a).arrayNode);
            } else{
                mapper.writeValue(filePath.toFile(), ((JSONJacksonArray) a).arrayNode);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFObject newObject() {
        return new JSONJacksonObject(this.mapper, this.mapper.createObjectNode());
    }

    public DSFObject parseObject(final String data) {
        try {
            return new JSONJacksonObject(this.mapper, this.mapper.readTree(data));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFObject parseObject(final Object object) {
        return new JSONJacksonObject(this.mapper, (JsonNode) object);
    }

    public DSFObject loadObject(final Path filePath) {
        try (BufferedReader reader = FileOps.createBufferedReaderUtfBOM(filePath)) {
            return new JSONJacksonObject(this.mapper, this.mapper.readTree(reader));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveObject(final DSFObject o, final Path filePath, final boolean pretty) {
        try {
            if (pretty) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), ((JSONJacksonObject) o).objectNode);
            } else{
                mapper.writeValue(filePath.toFile(), ((JSONJacksonObject) o).objectNode);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
