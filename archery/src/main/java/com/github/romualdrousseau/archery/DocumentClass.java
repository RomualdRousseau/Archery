package com.github.romualdrousseau.archery;

public interface DocumentClass {

    enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    Priority getPriority();

    Document newInstance();
}
