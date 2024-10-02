package com.github.romualdrousseau.archery.loader.excel.xls;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentClass;

public class XlsClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    public Document newInstance() {
        return new XlsDocument();
    }
}
