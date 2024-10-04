package com.github.romualdrousseau.archery.commons.dsf.yaml.jackson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFFactory;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;

public class YAMLJacksonFactory implements DSFFactory {
    private final ObjectMapper mapper;

    public YAMLJacksonFactory() {
        this.mapper = YAMLMapper.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        mapper.setDefaultPrettyPrinter(prettyPrinter);

        final StreamReadConstraints streamReadConstraints = StreamReadConstraints
                .builder()
                .maxStringLength(Integer.MAX_VALUE)
                .build();
        this.mapper.getFactory().setStreamReadConstraints(streamReadConstraints);
    }

    public DSFArray newArray() {
        return new YAMLJacksonArray(this.mapper, this.mapper.createArrayNode());
    }

    public DSFArray parseArray(final String data) {
        try {
            return new YAMLJacksonArray(this.mapper, this.mapper.readTree(data));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFArray parseArray(final Object object) {
        return new YAMLJacksonArray(this.mapper, (JsonNode) object);
    }

    public DSFArray loadArray(final Path filePath) {
        try (BufferedReader reader = this.createReader(filePath)) {
            return new YAMLJacksonArray(this.mapper, this.mapper.readTree(reader));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveArray(final DSFArray a, final Path filePath, final boolean pretty) {
        try {
            final var aa = (YAMLJacksonArray) a;
            if (pretty) {
                this.mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), aa.getJsonNode());
            } else {
                this.mapper.writeValue(filePath.toFile(), aa.getJsonNode());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFObject newObject() {
        return new YAMLJacksonObject(this.mapper, this.mapper.createObjectNode());
    }

    public DSFObject parseObject(final String data) {
        try {
            return new YAMLJacksonObject(this.mapper, this.mapper.readTree(data));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DSFObject parseObject(final Object object) {
        return new YAMLJacksonObject(this.mapper, (JsonNode) object);
    }

    public DSFObject loadObject(final Path filePath) {
        try (BufferedReader reader = this.createReader(filePath)) {
            return new YAMLJacksonObject(this.mapper, this.mapper.readTree(reader));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveObject(final DSFObject o, final Path filePath, final boolean pretty) {
        try {
            final var oo = (YAMLJacksonObject) o;
            if (pretty) {
                this.mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), oo.getJsonNode());
            } else {
                this.mapper.writeValue(filePath.toFile(), oo.getJsonNode());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BufferedReader createReader(final Path filePath) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8));

        // consume the Unicode BOM (byte order marker) if present
        reader.mark(1);
        final int c = reader.read();
        // if not the BOM, back up to the beginning again
        if (c != '\uFEFF') {
            reader.reset();
        }

        return reader;
    }
}
