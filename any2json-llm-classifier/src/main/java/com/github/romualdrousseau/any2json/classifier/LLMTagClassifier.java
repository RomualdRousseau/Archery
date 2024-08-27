package com.github.romualdrousseau.any2json.classifier;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.TagClassifier;

public class LLMTagClassifier extends SimpleTagClassifier {

    public LLMTagClassifier(final List<String> vocabulary, final int ngrams, final int wordMinSize,
            final List<String> lexicon, final Optional<Path> modelPath) {
    }

    public LLMTagClassifier(final Model model, final TagClassifier.TagStyle tagStyle) {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void updateModelData() {
    }

    @Override
    public String predict(final String name, final List<String> entities, final List<String> context) {
        return "none";
    }
}
