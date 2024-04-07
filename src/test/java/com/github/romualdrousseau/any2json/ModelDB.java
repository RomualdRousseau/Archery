package com.github.romualdrousseau.any2json;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class ModelDB {

    public static Model createConnection(final String modelName) {
        return new ModelBuilder()
                .fromPath(ModelDB.getResourcePath(String.format("/data/%s.json", modelName)))
                .build();
    }

    private static Path getResourcePath(final String resourceName) {
        try {
            final URL resourceUrl = new ModelDB().getClass().getResource(resourceName);
            assert resourceUrl != null : resourceName + " not found";
            return Path.of(resourceUrl.toURI());
        } catch (final URISyntaxException x) {
            assert false : x.getMessage();
            return null;
        }
    }
}
