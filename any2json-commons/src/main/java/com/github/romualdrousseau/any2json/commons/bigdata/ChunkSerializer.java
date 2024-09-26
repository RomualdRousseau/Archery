package com.github.romualdrousseau.any2json.commons.bigdata;

import java.io.IOException;

public interface ChunkSerializer {

    byte[] serialize(Row[] batch) throws IOException;

    Row[] deserialize(byte[] bytes) throws IOException;
}
