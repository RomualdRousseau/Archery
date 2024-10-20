package com.github.romualdrousseau.archery.commons.dsf.yaml.jackson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;

public class YAMLJacksonObject implements DSFObject {
    private final ObjectMapper mapper;
    private final ObjectNode objectNode;

    public YAMLJacksonObject(final ObjectMapper mapper, final JsonNode node) {
        this.mapper = mapper;
        if (node == null) {
            this.objectNode = mapper.createObjectNode();
        } else {
            this.objectNode = (ObjectNode) node;
        }
    }

    protected JsonNode getJsonNode() {
        return this.objectNode;
    }

    @Override
    public Iterable<String> keys() {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator()
            {
                return YAMLJacksonObject.this.objectNode.fieldNames();
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final String k) {
        final JsonNode node = this.objectNode.get(k);
        if (node == null) {
            return Optional.empty();
        }
        final T object;
        if (node.isObject()) {
            object = (T) new YAMLJacksonObject(this.mapper, node);
        } else if (node.isArray()) {
            object = (T) new YAMLJacksonArray(this.mapper, node);
        } else if (node.isInt()) {
            object = (T) Integer.valueOf(node.intValue());
        } else if (node.isFloat()) {
            object = (T) Float.valueOf(node.floatValue());
        } else if (node.isBoolean()) {
            object = (T) Boolean.valueOf(node.booleanValue());
        } else {
            object = (T) node.textValue();
        }
        return Optional.ofNullable(object);
    }

    @Override
    public <T> DSFObject set(final String k, final T o) {
        if (o instanceof DSFObject) {
            this.objectNode.set(k, ((YAMLJacksonObject) o).getJsonNode());
        } else if (o instanceof DSFArray) {
            this.objectNode.set(k, ((YAMLJacksonArray) o).getJsonNode());
        } else {
            this.objectNode.set(k, this.mapper.convertValue(o, JsonNode.class));
        }
        return this;
    }

    @Override
    public DSFObject remove(final String k) {
        this.objectNode.remove(k);
        return this;
    }

    public String toString(final boolean pretty) {
        try {
            if (pretty) {
                return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.getJsonNode());
            } else {
                return this.mapper.writeValueAsString(this.getJsonNode());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        //return this.objectNode.toString();
    }

    @Override
    public String toString() {
        return this.objectNode.toString();
    }
}
