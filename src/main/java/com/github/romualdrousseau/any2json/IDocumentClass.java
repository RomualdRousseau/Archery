package com.github.romualdrousseau.any2json;

public interface IDocumentClass
{
    enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    Priority getPriority();

	Document newInstance();
}
