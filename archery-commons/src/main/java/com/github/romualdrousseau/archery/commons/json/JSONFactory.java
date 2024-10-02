package com.github.romualdrousseau.archery.commons.json;

import java.nio.file.Path;

public interface JSONFactory {

    JSONArray newArray();

    JSONArray parseArray(String data);

    JSONArray parseArray(Object object);

    JSONArray loadArray(Path filePath);

    void saveArray(JSONArray a, Path filePath, boolean pretty);

    JSONObject newObject();

    JSONObject parseObject(String data);

    JSONObject parseObject(Object object);

    JSONObject loadObject(Path filePath);

    void saveObject(JSONObject o, Path filePath, boolean pretty);
}
