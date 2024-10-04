package com.github.romualdrousseau.archery.parser;

import com.github.romualdrousseau.archery.TableParser;
import com.github.romualdrousseau.archery.TableParserClass;
import com.github.romualdrousseau.archery.Model;

public class LayexTableParserClass implements TableParserClass {

    public TableParser newInstance(final Model model, final String parserOptions) {
        return new LayexTableParser(model, parserOptions);
    }
}
