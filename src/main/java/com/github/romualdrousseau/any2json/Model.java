package com.github.romualdrousseau.any2json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.any2json.base.ModelData;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.types.Tensor;

public class Model {

    public static final Model Default = new ModelBuilder().build();

    public Model(final ModelData modelData) {
        this(modelData, new HashMap<String, String>());
    }

    public Model(final ModelData modelData, final Map<String, String> modelAttributes) {
        this.modelData = modelData;
        this.attributes = modelAttributes;
        this.entities = modelData.getList("entities");
        this.patterns = modelData.getMap("patterns");
        this.filters = modelData.getList("filters");
        this.pivotEntities = modelData.getList("pivotEntityList");
        this.tags = modelData.getList("tags");
        this.requiredTags = modelData.getList("requiredTags");
        this.comparer = new RegexComparer(this.patterns);
    }

    public ModelData getData() {
        return modelData;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
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

    public String toEntityName(final String value, final String entityName) {
        return this.comparer.anonymize(value, entityName);
    }

    public Optional<String> toEntityValue(final String value) {
        return this.toEntityValueCache.computeIfAbsent(value, this.comparer::find);
    }

    public Optional<String> toEntityValue(final String value, final String entityName) {
        return this.comparer.find(value, entityName);
    }

    public Tensor toEntityVector(final String value) {
        return this.toEntityVectorCache.computeIfAbsent(value, v -> Tensor
                .of(Text.to_categorical(v, this.entities, this.comparer).stream().mapToDouble(x -> x).toArray()));
    }

    private final ModelData modelData;
    private final Map<String, String> attributes;
    private final List<String> entities;
    private final Map<String, String> patterns;
    private final List<String> filters;
    private final List<String> pivotEntities;
    private final List<String> tags;
    private final List<String> requiredTags;
    private final RegexComparer comparer;

    private LRUMap<String, Optional<String>> toEntityValueCache = new LRUMap<>();
    private LRUMap<String, Tensor> toEntityVectorCache = new LRUMap<>();
}
