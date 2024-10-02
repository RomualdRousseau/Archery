package com.github.romualdrousseau.archery.commons.yaml;

import java.nio.file.Path;

public interface YAMLFactory {

    YAMLArray newArray();

    YAMLArray parseArray(String data);

    YAMLArray parseArray(Object object);

    YAMLArray loadArray(Path filePath);

    void saveArray(YAMLArray a, Path filePath, boolean pretty);

    YAMLObject newObject();

    YAMLObject parseObject(String data);

    YAMLObject parseObject(Object object);

    YAMLObject loadObject(Path filePath);

    void saveObject(YAMLObject o, Path filePath, boolean pretty);
}
