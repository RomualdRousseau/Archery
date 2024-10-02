package com.github.romualdrousseau.archery.modeldata;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.archery.ModelData;
import com.github.romualdrousseau.archery.commons.types.Pair;
import com.github.romualdrousseau.archery.commons.yaml.YAML;
import com.github.romualdrousseau.archery.commons.yaml.YAMLArray;
import com.github.romualdrousseau.archery.commons.yaml.YAMLObject;

public class DataContractModelData implements ModelData {

    public static DataContractModelData empty() {
        return new DataContractModelData(null);
    }

    public DataContractModelData(final YAMLObject backstore) {
        this.backstore = backstore;

        if (backstore == null) {
            return;
        }

        final var entities = this.backstore.<YAMLObject>get("entities").get();

        this.entityList = StreamSupport.stream(entities.keys().spliterator(), false).toList();

        this.patternMap = StreamSupport.stream(entities.keys().spliterator(), false).flatMap(k -> {
            final var entity = entities.<YAMLObject>get(k).get();
            final var patterns = entity.<YAMLArray>get("patterns").get();
            return patterns.<String>stream().map(v -> new Pair(v, k));
        }).collect(Collectors.toUnmodifiableMap(s -> s.getKey(), s -> s.getValue()));

        this.pivotEntityList = StreamSupport.stream(entities.keys().spliterator(), false).filter(k -> {
            final var entity = entities.<YAMLObject>get(k).get();
            return entity.<Boolean>get("pivot").orElse(false);
        }).toList();

        this.filterList = YAML.<String>queryStream(this.backstore, "extracts.cleanser.filters").toList();
        this.recipe =  YAML.<String>queryStream(this.backstore, "extracts.cleanser.recipe").toList();

        this.metaLayexList = YAML.<String>queryStream(this.backstore, "extracts.parser.meta").toList();
        this.dataLayexList = YAML.<String>queryStream(this.backstore, "extracts.parser.data").toList();

        final var tags = this.backstore.<YAMLObject>get("definitions").get();

        this.tagList = StreamSupport.stream(tags.keys().spliterator(), false).toList();

        this.requiredTagList = StreamSupport.stream(tags.keys().spliterator(), false).filter(k -> {
            final var tag = tags.<YAMLObject>get(k).get();
            return tag.<Boolean>get("required").orElse(false);
        }).toList();

        this.definitions = List.of(tags.toString(false).split("\n"));

        this.lexicon = Collections.emptyList();
    }

    @Override
    public <T> Optional<T> get(final String key) {
        return Optional.empty();
    }

    @Override
    public <T> ModelData set(final String key, final T value) {
        return this;
    }

    @Override
    public List<String> getList(final String key) {
        if ("definitions".equals(key)) {
            return this.definitions;
        } else if ("recipe".equals(key)) {
            return this.recipe;
        } else if ("entities".equals(key)) {
            return this.entityList;
        } else if ("filters".equals(key)) {
            return this.filterList;
        } else if ("pivotEntityList".equals(key)) {
            return this.pivotEntityList;
        } else if ("metaLayexes".equals(key)) {
            return this.metaLayexList;
        } else if ("dataLayexes".equals(key)) {
            return this.dataLayexList;
        } else if ("tags".equals(key)) {
            return this.tagList;
        } else if ("requiredTags".equals(key)) {
            return this.requiredTagList;
        } else if ("lexicon".equals(key)) {
            return this.lexicon;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ModelData setList(final String key, final List<String> values) {
        if ("metaLayexes".equals(key)) {
            this.metaLayexList = values;
        } else if ("dataLayexes".equals(key)) {
            this.dataLayexList = values;
        } else if ("lexicon".equals(key)) {
            this.lexicon = values;
        }
        return this;
    }

    @Override
    public Map<String, String> getMap(final String key) {
        if ("patterns".equals(key)) {
            return this.patternMap;
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public ModelData setMap(final String key, final Map<String, String> values) {
        return this;
    }

    @Override
    public void save(final Path path) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    private final YAMLObject backstore;
    private List<String> filterList;
    private List<String> entityList;
    private Map<String, String> patternMap;
    private List<String> pivotEntityList;
    private List<String> recipe;
    private List<String> metaLayexList;
    private List<String> dataLayexList;
    private List<String> tagList;
    private List<String> requiredTagList;
    private List<String> definitions;
    private List<String> lexicon;
}
