package com.github.romualdrousseau.archery;

import java.util.List;

public interface TagClassifier extends AutoCloseable {

    enum TagStyle {
        NONE,
        CAMEL,
        SNAKE
    }

    Model getModel();

    TagClassifier setModel(final Model model);

    String predict(final Table table, final Header header);

    TagClassifier.TagStyle getTagStyle();

    TagClassifier setTagStyle(final TagClassifier.TagStyle mode);

    List<String> getLexicon();

    TagClassifier setLexicon(final List<String> lexion);

    String ensureTagStyle(final String text);
}
