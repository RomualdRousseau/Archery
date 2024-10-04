package com.github.romualdrousseau.archery.commons.dsf.yaml.jackson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;

public class YAMLJacksonArray implements DSFArray {
    private final ObjectMapper mapper;
    private final ArrayNode arrayNode;

    public YAMLJacksonArray(final ObjectMapper mapper, final JsonNode node) {
        this.mapper = mapper;
        if (node == null) {
            this.arrayNode = mapper.createArrayNode();
        } else {
            this.arrayNode = (ArrayNode) node;
        }
    }

    protected JsonNode getJsonNode() {
        return this.arrayNode;
    }

    @Override
    public int size() {
        return (this.arrayNode == null) ? 0 : this.arrayNode.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final int i) {
        final var node = this.arrayNode.get(i);
        if (node == null) {
            return Optional.empty();
        }
        final T object;
        if (node.isObject()) {
            object =  (T) new YAMLJacksonObject(this.mapper, node);
        } else if (node.isArray()) {
            object = (T) new YAMLJacksonArray(this.mapper, node);
        } else if (node.isInt()) {
            object = (T) Integer.valueOf(node.intValue());
        } else if (node.isFloat()) {
            object = (T) Float.valueOf(node.floatValue());
        } else {
            object = (T) node.textValue();
        }
        return Optional.ofNullable(object);
    }

    @Override
    public <T> DSFArray set(final int i, final T o) {
        if (o instanceof DSFObject) {
            this.arrayNode.set(i, ((YAMLJacksonObject) o).getJsonNode());
        } else if (o instanceof DSFArray) {
            this.arrayNode.set(i, ((YAMLJacksonArray) o).getJsonNode());
        } else {
            this.arrayNode.set(i, this.mapper.convertValue(o, JsonNode.class));
        }
        return this;
    }

    @Override
    public <T> DSFArray append(final T o) {
        if (o instanceof DSFObject) {
            this.arrayNode.add(((YAMLJacksonObject) o).getJsonNode());
        } else if (o instanceof DSFArray) {
            this.arrayNode.add(((YAMLJacksonArray) o).getJsonNode());
        } else if (o instanceof Integer) {
            this.arrayNode.add((Integer) o);
        } else if (o instanceof Float) {
            this.arrayNode.add((Float) o);
        } else {
            this.arrayNode.add(o.toString());
        }
        return this;
    }

    @Override
    public DSFArray remove(final int i) {
        this.arrayNode.remove(i);
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
        return this.arrayNode.toString();
    }
}
