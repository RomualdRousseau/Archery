package com.github.romualdrousseau.any2json;

import java.nio.file.Path;
import java.net.URL;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.romualdrousseau.shuju.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testReadVariousDocuments() {
        final JSONObject model = ModelDB.createConnection("sales-english");

        try (final Document doc = this.loadDocument("/data/HongKong - ZUELLIG - Sales - 20220305.xlsx", "UTF-8")) {
            // doc.setModel(model);
            // doc.setHints(INTELLI_LAYOUT | INTELLI_TAG);
            // doc.load();
            // doc.sheets.forEach(s -> s.table().ifPresent(t -> t.headers().forEach(h -> h.values().forEach(v -> v.getValue()))))
        }
    }

    private Document loadDocument(String resourceName, String encoding) {
        return DocumentFactory.createInstance(this.getResourcePath(resourceName).toFile(), encoding);
    }

    private Path getResourcePath(String resourceName) {
        try {
            URL resourceUrl = this.getClass().getResource(resourceName);
            assert resourceUrl != null : resourceName + " not found";
            return Path.of(resourceUrl.toURI());
        } catch (URISyntaxException x) {
            assert false : x.getMessage();
            return null;
        }
    }
}
