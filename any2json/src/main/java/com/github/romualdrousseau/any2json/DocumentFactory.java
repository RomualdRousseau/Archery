package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.config.DynamicPackages;

public class DocumentFactory {

    public static Document createInstance(final String filePath, final String encoding) {
        return DocumentFactory.createInstance(new File(filePath), encoding, null, null);
    }

    public static Document createInstance(final String filePath, final String encoding, final String password) {
        return DocumentFactory.createInstance(new File(filePath), encoding, password, null);
    }

    public static Document createInstance(final File file, final String encoding) {
        return DocumentFactory.createInstance(file, encoding, null, null);
    }

    public static Document createInstance(final File file, final String encoding, final String password, final String sheetName) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        return DynamicPackages.GetDocumentFactories().stream()
                .map(DocumentClass::newInstance)
                .filter(x -> x.open(file, encoding, password, sheetName))
                .findFirst()
                .orElseThrow(() -> new UnknownFormatConversionException(file.toString()));
    }
}
