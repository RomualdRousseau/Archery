package com.github.romualdrousseau.any2json.classifier;

import com.github.romualdrousseau.any2json.TagClassifier;
import com.github.romualdrousseau.any2json.TagClassifierClass;
import com.github.romualdrousseau.any2json.Model;

public class NetTagClassifierClass implements TagClassifierClass {

    @Override
    public TagClassifier newInstance(Model model, final TagClassifier.TagStyle tagStyle) {
        return new NetTagClassifier(model, tagStyle);
    }
}
