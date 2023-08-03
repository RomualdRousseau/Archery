package com.github.romualdrousseau.any2json.loader.excel.xls;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.IDocumentClass;

public class XlsClass implements IDocumentClass
{
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    public Document newInstance() {
        return new XlsDocument();
    }
}
