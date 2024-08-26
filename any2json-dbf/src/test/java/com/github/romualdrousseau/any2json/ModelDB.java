package com.github.romualdrousseau.any2json;

import java.io.IOException;
import java.net.URISyntaxException;

public class ModelDB {

    public static Model createConnection(final String modelName) {
        try {
            return new ModelBuilder()
                    .fromResource(new ModelDB().getClass(), String.format("/data/%s.json", modelName))
                    .build();
        } catch (final URISyntaxException | IOException x) {
            assert false : x.getMessage();
            return null;
        }
    }
}
