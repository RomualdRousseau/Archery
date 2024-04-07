package com.github.romualdrousseau.any2json;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.types.Tensor;

public class Model {

    public static final Model Default = new ModelBuilder().build();

    public Model(final JSONObject jsonModel) {
        this.jsonModel = jsonModel;
        this.entities = JSON.<String>streamOf(jsonModel.getArray("entities")).collect(Collectors.toUnmodifiableList());
        this.patterns = JSON.<JSONObject>streamOf(jsonModel.getArray("patterns"))
                .collect(Collectors.toUnmodifiableMap(x -> x.getString("key"), x -> x.getString("value")));
        this.filters = JSON.<String>streamOf(jsonModel.getArray("filters")).collect(Collectors.toUnmodifiableList());
        this.pivotEntities = JSON.<String>streamOf(jsonModel.getArray("pivotEntityList"))
                .collect(Collectors.toUnmodifiableList());
        this.tags = JSON.<String>streamOf(jsonModel.getArray("tags")).collect(Collectors.toUnmodifiableList());
        this.requiredTags = JSON.<String>streamOf(jsonModel.getArray("requiredTags"))
                .collect(Collectors.toUnmodifiableList());
        this.comparer = new RegexComparer(this.patterns);
    }

    public List<String> getEntityList() {
        return this.entities;
    }

    public Map<String, String> getPatternMap() {
        return this.patterns;
    }

    public List<String> getFilters() {
        return this.filters;
    }

    public List<String> getPivotEntityList() {
        return this.pivotEntities;
    }

    public List<String> getTagList() {
        return this.tags;
    }

    public List<String> getRequiredTagList() {
        return this.requiredTags;
    }

    public String toEntityName(final String value) {
        return this.comparer.anonymize(value);
    }

    public Optional<String> toEntityValue(final String value) {
        return this.comparer.find(value);
    }

    public Tensor toEntityVector(final String value) {
        return Tensor.of(Text.to_categorical(value, this.entities, this.comparer).stream()
                .mapToDouble(x -> (double) x).toArray());
    }

    public JSONObject toJSON() {
        return jsonModel;
    }

    private final JSONObject jsonModel;
    private final List<String> entities;
    private final Map<String, String> patterns;
    private final List<String> filters;
    private final List<String> pivotEntities;
    private final List<String> tags;
    private final List<String> requiredTags;
    private final RegexComparer comparer;
}
