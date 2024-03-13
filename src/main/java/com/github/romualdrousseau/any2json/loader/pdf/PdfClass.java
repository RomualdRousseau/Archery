package com.github.romualdrousseau.any2json.loader.pdf;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentClass;

public class PdfClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.LOW;
    }

    public Document newInstance() {
        return new PdfDocument();
    }
}
