package com.github.romualdrousseau.archery.loader.excel.xml;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentClass;

public class XmlClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new XmlDocument();
    }
}
