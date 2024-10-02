package com.github.romualdrousseau.archery;

import java.io.IOException;
import java.net.URISyntaxException;

import com.github.romualdrousseau.archery.modeldata.JsonModelBuilder;

public class ModelDB {

    public static Model createConnection(final String modelName) {
        try {
            return new JsonModelBuilder()
                    .fromResource(ModelDB.class, String.format("/data/%s.json", modelName))
                    .build();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
