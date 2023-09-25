package com.github.romualdrousseau.any2json.classifier;

import java.util.List;

import com.github.romualdrousseau.shuju.json.JSONObject;

public class TrainingConflict {

    private JSONObject document;
    public JSONObject getDocument() {
        return document;
    }

    private List<String> name;
    public List<String> getName() {
        return name;
    }

    private String tag;
    public String getTag() {
        return tag;
    }

    public static TrainingConflict of(JSONObject document, List<String> name, String tag) {
        final var result = new TrainingConflict();
        result.document = document;
        result.name = name;
        result.tag = tag;
        return result;
    }
}
