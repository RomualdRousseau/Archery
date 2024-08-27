package com.github.romualdrousseau.any2json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.any2json.modeldata.EmptyModelData;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;
import com.github.romualdrousseau.shuju.types.Tensor;

public class Model {

    public static final ThreadLocal<Model> Default = new ThreadLocal<>() {
        @Override
        protected Model initialValue() {
            return new Model(EmptyModelData.empty());
        }
    };

    public Model(final ModelData modelData) {
        this(modelData, new HashMap<String, String>());
    }

    public Model(final ModelData modelData, final Map<String, String> modelAttributes) {
        this.modelData = modelData;
        this.modelAttributes = modelAttributes;
        this.entityList = modelData.getList("entities");
        this.patternMap = modelData.getMap("patterns");
        this.filterList = modelData.getList("filters");
        this.pivotEntityList = modelData.getList("pivotEntityList");
        this.tagList = modelData.getList("tags");
        this.requiredTagList = modelData.getList("requiredTags");
        this.comparer = new RegexComparer(this.patternMap);
    }

    public ModelData getData() {
        return modelData;
    }

    public Map<String, String> getModelAttributes() {
        return this.modelAttributes;
    }

    public List<String> getEntityList() {
        return this.entityList;
    }

    public Map<String, String> getPatternMap() {
        return this.patternMap;
    }

    public List<String> getFilterList() {
        return this.filterList;
    }

    public List<String> getPivotEntityList() {
        return this.pivotEntityList;
    }

    public List<String> getTagList() {
        return this.tagList;
    }

    public List<String> getRequiredTagList() {
        return this.requiredTagList;
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
                .of(Text.to_categorical(v, this.entityList, this.comparer).stream().mapToDouble(x -> x).toArray()));
    }

    private final ModelData modelData;
    private final Map<String, String> modelAttributes;
    private final List<String> entityList;
    private final Map<String, String> patternMap;
    private final List<String> filterList;
    private final List<String> pivotEntityList;
    private final List<String> tagList;
    private final List<String> requiredTagList;
    private final RegexComparer comparer;

    private final LRUMap<String, Optional<String>> toEntityValueCache = new LRUMap<>();
    private final LRUMap<String, Tensor> toEntityVectorCache = new LRUMap<>();
}
