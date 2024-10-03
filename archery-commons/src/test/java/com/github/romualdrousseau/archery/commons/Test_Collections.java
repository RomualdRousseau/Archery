package com.github.romualdrousseau.archery.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.github.romualdrousseau.archery.commons.collections.ChunkSerializer;
import com.github.romualdrousseau.archery.commons.collections.ChunkSerializerFactory;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.collections.ChunkSerializerFactory.SerializerType;

public class Test_Collections {

    private final ChunkSerializer serializer = ChunkSerializerFactory.newInstance(SerializerType.FURY);

    @Test
    @Tag("unit")
    public void testSerialize() throws IOException {
        final var rows = IntStream.range(0, 10000)
                .mapToObj(i -> Row.of(IntStream.range(0, 1000)
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)))
                .toArray(Row[]::new);
        final var bytes = serializer.serialize(rows);
        System.out.println(bytes.length >> 20);

        final var rows2 = serializer.deserialize(bytes);
        assertEquals(rows.length, rows2.length);
        Arrays.stream(rows2).forEach(row -> {
            row.forEach(x -> {
                assertEquals("nisl purus in mollis nunc", x);
            });
        });
    }

    @Test
    @Tag("unit")
    public void testDataFrameWhole() throws IOException {
        try (final var writer = new DataFrameWriter(100, 10)) {
            for (int i = 0; i < 10; i++) {
                writer.write(Row.of(IntStream.range(0, writer.getColumnCount())
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)));
            }
            try (final var df = writer.getDataFrame()) {
                df.forEach(y -> {
                    y.forEach(x -> {
                        assertEquals("nisl purus in mollis nunc", x);
                    });
                });
            }
        }
    }

    @Test
    @Tag("unit")
    public void testDataFrameFullView() throws IOException {
        try (final var writer = new DataFrameWriter(100, 10)) {
            for (int i = 0; i < 10; i++) {
                writer.write(Row.of(IntStream.range(0, writer.getColumnCount())
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)));
            }
            try (final var df = writer.getDataFrame()) {
                final var view = df.view(0, 0, 10, 10);
                view.forEach(y -> {
                    y.forEach(x -> {
                        assertEquals("nisl purus in mollis nunc", x);
                    });
                });
            }
        }
    }

    @Test
    @Tag("unit")
    public void testDataFramePartialView() throws IOException {
        try (final var writer = new DataFrameWriter(100, 100)) {
            for (int i = 0; i < 5000; i++) {
                writer.write(Row.of(IntStream.range(0, writer.getColumnCount())
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)));
            }
            try (final var df = writer.getDataFrame()) {
                final var view = df.view(2000, 50, 10, 10);
                view.forEach(y -> {
                    y.forEach(x -> {
                        assertEquals("nisl purus in mollis nunc", x);
                    });
                });
            }
        }
    }

    @Test
    @Tag("unit")
    public void testDataFrameRandom() throws IOException {
        try (final var writer = new DataFrameWriter(100, 100)) {
            for (int i = 0; i < 5000; i++) {
                writer.write(Row.of(IntStream.range(0, writer.getColumnCount())
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)));
            }
            try (final var df = writer.getDataFrame()) {
                assertEquals("nisl purus in mollis nunc", df.getCell(0, 5));
                assertEquals("nisl purus in mollis nunc", df.getCell(5, 8));
            }
        }
    }

    @Test
    @Tag("full")
    public void testDataFrameMassive() throws IOException {
        try (final var writer = new DataFrameWriter(10000, 1000)) {
            for (int i = 0; i < 10000000; i++) {
                writer.write(Row.of(IntStream.range(0, writer.getColumnCount())
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new)));
            }
            try (final var df = writer.getDataFrame()) {
                df.forEach(y -> {
                    y.forEach(x -> {
                        assertEquals("nisl purus in mollis nunc", x);
                    });
                });
            }
        }
    }

    @Test
    @Tag("full")
    public void testArrayListMassive() {
        assertThrows(OutOfMemoryError.class, () -> {
            final var list = new ArrayList<String[]>();
            for (int i = 0; i < 10000000; i++) {
                list.add(IntStream.range(0, 1000)
                        .mapToObj(j -> "nisl purus in mollis nunc")
                        .toArray(String[]::new));
            }
            list.forEach(y -> {
                List.of(y).forEach(x -> {
                    assertEquals("nisl purus in mollis nunc", x);
                });
            });
        });
    }
}
