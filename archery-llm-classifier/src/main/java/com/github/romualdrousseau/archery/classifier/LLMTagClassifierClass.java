package com.github.romualdrousseau.archery.classifier;

import com.github.romualdrousseau.archery.TagClassifier;
import com.github.romualdrousseau.archery.TagClassifierClass;
import com.github.romualdrousseau.archery.Model;

public class LLMTagClassifierClass implements TagClassifierClass {

    @Override
    public TagClassifier newInstance(Model model, final TagClassifier.TagStyle tagStyle) {
        return new LLMTagClassifier(model, tagStyle);
    }
}
