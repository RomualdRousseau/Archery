package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.modeldata.DataContractModelBuilder;

/**
 * Unit test for the TrainingEntry class.
 */
public class Test_LLM {

    @Test
    public void testDocumentPanama() throws IOException, URISyntaxException {
        final var model = new DataContractModelBuilder()
                .fromResource(getClass(), "/data-contract-lowes.yml")
                .setLexicon("english")
                .build();

        final var file = this.getResourcePath("/data/Panama - BH - LOWES - Sales - 20201201.xlsx");
        try (final var doc = DocumentFactory.createInstance(file.toFile(), "UTF-8")
                .setHints(EnumSet.of(Document.Hint.INTELLI_TAG))
                .setModel(model)) {
            doc.getSheetAt(0).getTable().ifPresent(t -> {
                final var csvContent = new StringBuilder();
                t.headers().forEach(h -> csvContent.append(h.getName() + " (" + h.getTag().getValue() + "),"));
                csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                StreamSupport.stream(t.rows().spliterator(), false).forEach(r -> {
                    if (r.getNumberOfCells() > 0) {
                        r.cells().forEach(c -> csvContent.append("\"" + c.getValue() + "\","));
                        csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                    }
                });
                System.out.println(csvContent.toString());
            });
        }

    }

    @Test
    public void testDocumentHongKongA() throws IOException, URISyntaxException {
        final var model = new DataContractModelBuilder()
                .fromResource(getClass(), "/data-contract-zuellig-a.yml")
                .setLexicon("english")
                .build();

        final var file = this.getResourcePath("/data/Malaysia - ZUELLIG - Sales - 20201002.xlsx");
        try (final var doc = DocumentFactory.createInstance(file.toFile(), "UTF-8")
                .setHints(EnumSet.of(Document.Hint.INTELLI_TAG))
                .setModel(model)) {
            doc.getSheetAt(0).getTable().ifPresent(t -> {
                final var csvContent = new StringBuilder();
                t.headers().forEach(h -> csvContent.append(h.getName() + " (" + h.getTag().getValue() + "),"));
                csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                StreamSupport.stream(t.rows().spliterator(), false).forEach(r -> {
                    if (r.getNumberOfCells() > 0) {
                        r.cells().forEach(c -> csvContent.append("\"" + c.getValue() + "\","));
                        csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                    }
                });
                System.out.println(csvContent.toString());
            });
        }

    }

    @Test
    public void testDocumentHongKongB() throws IOException, URISyntaxException {
        final var model = new DataContractModelBuilder()
                .fromResource(getClass(), "/data-contract-zuellig-b.yml")
                .setLexicon("english")
                .build();

        final var file = this.getResourcePath("/data/Malaysia - ZUELLIG - Sales - 20201002.xlsx");
        try (final var doc = DocumentFactory.createInstance(file.toFile(), "UTF-8")
                .setHints(EnumSet.of(Document.Hint.INTELLI_TAG))
                .setModel(model)) {
            doc.getSheetAt(0).getTable().ifPresent(t -> {
                final var csvContent = new StringBuilder();
                t.headers().forEach(h -> csvContent.append(h.getName() + " (" + h.getTag().getValue() + "),"));
                csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                StreamSupport.stream(t.rows().spliterator(), false).forEach(r -> {
                    if (r.getNumberOfCells() > 0) {
                        r.cells().forEach(c -> csvContent.append("\"" + c.getValue() + "\","));
                        csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
                    }
                });
                System.out.println(csvContent.toString());
            });
        }

    }

    private Path getResourcePath(String resourceName) {
        try {
            URL resourceUrl = this.getClass().getResource(resourceName);
            assert resourceUrl != null : resourceName + " not found";
            return Path.of(resourceUrl.toURI());
        } catch (URISyntaxException x) {
            throw new RuntimeException(x);
        }
    }
}
