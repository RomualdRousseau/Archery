package com.github.romualdrousseau.archery.loader.dbf;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentClass;

public class DbfClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new DbfDocument();
    }
}
