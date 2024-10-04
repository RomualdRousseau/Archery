package com.github.romualdrousseau.archery.commons.collections;

import java.io.IOException;

public interface ChunkSerializer {

    byte[] serialize(Row[] batch) throws IOException;

    Row[] deserialize(byte[] bytes) throws IOException;
}
