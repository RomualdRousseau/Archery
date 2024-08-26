package com.github.romualdrousseau.any2json.loader.excel.xml;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentClass;

public class XmlClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new XmlDocument();
    }
}
