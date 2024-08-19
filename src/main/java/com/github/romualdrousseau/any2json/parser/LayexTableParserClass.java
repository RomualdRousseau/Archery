package com.github.romualdrousseau.any2json.parser;

import com.github.romualdrousseau.any2json.TableParser;
import com.github.romualdrousseau.any2json.TableParserClass;
import com.github.romualdrousseau.any2json.Model;

public class LayexTableParserClass implements TableParserClass {

    public TableParser newInstance(final Model model, final String parserOptions) {
        return new LayexTableParser(model, parserOptions);
    }
}
