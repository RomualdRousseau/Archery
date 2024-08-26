package com.github.romualdrousseau.any2json.loader.excel.xls;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentClass;

public class XlsClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    public Document newInstance() {
        return new XlsDocument();
    }
}
