package com.github.romualdrousseau.archery.examples;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.modeldata.JsonModelBuilder;
import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.archery.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.archery.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.archery.event.TableReadyEvent;

public class Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(Common.class);
    private static final String REPO_BASE_URL = "https://raw.githubusercontent.com/romualdrousseau/archery/main/archery-models";

    public static <T> JsonModelBuilder loadModelBuilder(final String modelName, final Class<T> clazz) {
        return new JsonModelBuilder().fromPath(Common.getResourcePath(String.format("/models/%s.json", modelName), clazz));
    }

    public static JsonModelBuilder loadModelBuilderFromGitHub(final String modelName) {
        try {
            LOGGER.info("Loaded model: " + modelName);
            final var url = String.format("%1$s/%2$s/%2$s.json", REPO_BASE_URL, modelName);
            return new JsonModelBuilder().fromURL(url);
        } catch (final IOException | InterruptedException x) {
            throw new RuntimeException(x);
        }
    }

    public static <T> File loadData(final String fileName, final Class<T> clazz) {
        return Common.getResourcePath(String.format("/data/%s", fileName), clazz).toFile();
    }

    public static <T> Path getResourcePath(final String resourceName, final Class<T> clazz) {
        try {
            final var resourceUrl = clazz.getResource(resourceName);
            if (resourceUrl == null) {
                throw new RuntimeException(resourceName + " not found");
            }
            LOGGER.info("Loaded resource: " + resourceName);
            return Path.of(resourceUrl.toURI());
        } catch (final URISyntaxException x) {
            throw new RuntimeException(x);
        }
    }

    public static void printHeaders(final Iterable<Header> headers) {
        headers.forEach(h -> System.out.print(String.format("%16.16s\t", h.getName())));
        System.out.println();
    }

    public static void printTags(final Iterable<Header> headers) {
        headers.forEach(h -> System.out.print(String.format("%16.16s\t", h.getTag().getValue())));
        System.out.println();
    }

    public static void printRows(final Iterable<Row> rows) {
        rows.forEach(r -> {
            r.cells().forEach(c -> System.out.print(String.format("%16.16s\t", c.getValue())));
            System.out.println();
        });
    }

    public static Sheet addSheetDebugger(final Sheet sheet) {
        sheet.addSheetListener(e -> {
            if (e instanceof BitmapGeneratedEvent) {
                LOGGER.debug("Extracting features ...");
            }

            if (e instanceof MetaTableListBuiltEvent) {
                LOGGER.debug("Generating Layout Graph ...");
            }

            if (e instanceof TableGraphBuiltEvent) {
                LOGGER.debug("Assembling Tabular Output ...");
                ((TableGraphBuiltEvent) e).dumpTableGraph(System.out);
            }

            if (e instanceof TableReadyEvent) {
                LOGGER.debug("Done.");
            }
        });
        return sheet;
    }
}
