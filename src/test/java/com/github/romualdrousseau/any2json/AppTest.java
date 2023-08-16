package com.github.romualdrousseau.any2json;

import java.nio.file.Path;
import java.util.EnumSet;
import java.net.URL;
import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testReadVariousDocuments() {
        final File file = this.getResourceFile("HongKong - ZUELLIG - Sales - 20220305.xlsx");

        final Model model = ModelDB.createConnection("sales-english");

        try (final Document doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT, Document.Hint.INTELLI_TAG))) {
            doc.sheets().forEach(s -> s.getTable().ifPresent(t -> t.headers().forEach(h -> System.out.println(h.getName()))));
        }
    }

    private File getResourceFile(String resourceName) {
        try {
            URL resourceUrl = this.getClass().getResource(String.format("/data/%s", resourceName));
            assert resourceUrl != null : resourceName + " not found";
            return Path.of(resourceUrl.toURI()).toFile();
        } catch (URISyntaxException x) {
            assert false : x.getMessage();
            return null;
        }
    }
}
