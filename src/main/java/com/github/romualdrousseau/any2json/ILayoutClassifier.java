package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.math.Tensor;

import java.util.List;
import java.util.Optional;

public interface ILayoutClassifier {

    int getSampleCount();

    List<String> getEntityList();

    List<String> getPivotEntityList();

    List<TableMatcher> getMetaMatcherList();

    void setMetaMatcherList(List<TableMatcher> matchers);

    List<TableMatcher> getDataMatcherList();

    void setDataMatcherList(List<TableMatcher> matchers);

    String getRecipe();

    void setRecipe(String recipe);

    String toEntityName(String value);

    Optional<String> toEntityValue(String value);

    Tensor toEntityVector(String value);

    JSONObject toJSON();
}
