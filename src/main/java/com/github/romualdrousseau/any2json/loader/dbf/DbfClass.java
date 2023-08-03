package com.github.romualdrousseau.any2json.loader.dbf;

import java.io.File;
import java.util.Optional;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.IDocumentClass;

public class DbfClass implements IDocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new DbfDocument();
    }
}
