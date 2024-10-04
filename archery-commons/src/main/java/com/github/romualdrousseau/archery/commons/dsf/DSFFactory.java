package com.github.romualdrousseau.archery.commons.dsf;

import java.nio.file.Path;

public interface DSFFactory {

    DSFArray newArray();

    DSFArray parseArray(String data);

    DSFArray parseArray(Object object);

    DSFArray loadArray(Path filePath);

    void saveArray(DSFArray a, Path filePath, boolean pretty);

    DSFObject newObject();

    DSFObject parseObject(String data);

    DSFObject parseObject(Object object);

    DSFObject loadObject(Path filePath);

    void saveObject(DSFObject o, Path filePath, boolean pretty);
}
