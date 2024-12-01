package com.github.romualdrousseau.archery.commons.dsf.yaml.jackson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

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
import com.github.romualdrousseau.archery.commons.io.FileOps;

public class YAMLJacksonFactory implements DSFFactory {

    private static final ThreadLocal<YAMLMapper> YAML_MAPPER = new ThreadLocal<>() {
        @Override
        protected YAMLMapper initialValue() {
            final var mapper = YAMLMapper.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .build();

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

    private final ObjectMapper mapper = YAML_MAPPER.get();

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
        try (BufferedReader reader = FileOps.createBufferedReaderUtfBOM(filePath)) {
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
        try (BufferedReader reader = FileOps.createBufferedReaderUtfBOM(filePath)) {
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
}
