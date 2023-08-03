package com.github.romualdrousseau.any2json.loader.excel.xlsx;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.IDocumentClass;

public class XlsxClass implements IDocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new XlsxDocument();
    }
}
