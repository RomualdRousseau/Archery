package com.github.romualdrousseau.archery.commons.bigdata;

import java.io.IOException;

public interface ChunkSerializer {

    byte[] serialize(Row[] batch) throws IOException;

    Row[] deserialize(byte[] bytes) throws IOException;
}
