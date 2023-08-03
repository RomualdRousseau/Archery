package com.github.romualdrousseau.any2json.loader.csv;

import java.io.File;
import java.util.Optional;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.IDocumentClass;

public class CsvClass implements IDocumentClass
{
    public Priority getPriority() {
        return Priority.HIGH;
    }

    public Document newInstance() {
        return new CsvDocument();
    }
}
