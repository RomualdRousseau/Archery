package com.github.romualdrousseau.archery.loader.csv;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentClass;

public class CsvClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.HIGH;
    }

    public Document newInstance() {
        return new CsvDocument();
    }
}
