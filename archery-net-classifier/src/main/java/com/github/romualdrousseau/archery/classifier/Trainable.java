package com.github.romualdrousseau.archery.classifier;

import java.util.List;

public interface Trainable {

    List<Integer> getInputVector(final String name, final List<String> entities, final List<String> context);

    List<Integer> getOutputVector(final String label);
}
