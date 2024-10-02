package com.github.romualdrousseau.archery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.romualdrousseau.archery.classifier.SimpleTagClassifier;

/**
 * Unit test for Archery.
 */
public class Test_Any2Json {

    class SimpleHeader implements Header {

        private final String name;

        public SimpleHeader(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Cell getCellAtRow(Row row) {
            throw new UnsupportedOperationException("Unimplemented method 'getCellAtRow'");
        }

        @Override
        public Cell getCellAtRow(Row row, boolean merged) {
            throw new UnsupportedOperationException("Unimplemented method 'getCellAtRow'");
        }

        @Override
        public boolean hasTag() {
            throw new UnsupportedOperationException("Unimplemented method 'hasTag'");
        }

        @Override
        public HeaderTag getTag() {
            throw new UnsupportedOperationException("Unimplemented method 'getTag'");
        }

        @Override
        public Iterable<String> entities() {
            throw new UnsupportedOperationException("Unimplemented method 'entities'");
        }

        @Override
        public String getEntitiesAsString() {
            throw new UnsupportedOperationException("Unimplemented method 'getEntitiesAsString'");
        }

        @Override
        public boolean isColumnEmpty() {
            throw new UnsupportedOperationException("Unimplemented method 'isColumnEmpty'");
        }

        @Override
        public boolean isColumnMerged() {
            throw new UnsupportedOperationException("Unimplemented method 'isColumnMerged'");
        }
    }

    private static final Model model;
    static {
        model = ModelDB.createConnection("sales-english");
    }

    @Test
    public void testSimpleClassifierWithEnclosedTag() throws Exception {
        try (final var classifer = new SimpleTagClassifier(model, TagClassifier.TagStyle.NONE)) {
            assertEquals("customerName", classifer.predict(null, new SimpleHeader("customer name ($customerName)")));
        }
    }

    @Test
    public void testSimpleClassifierWithSnake() throws Exception {
        try (final var classifer = new SimpleTagClassifier(model, TagClassifier.TagStyle.SNAKE)) {
            assertEquals("customer_name_u", classifer.predict(null, new SimpleHeader("customer name_u")));
            assertEquals("customer_name_u", classifer.predict(null, new SimpleHeader("customer_name_u")));
            assertEquals("customer_name_u", classifer.predict(null, new SimpleHeader("customerName_u")));
            assertEquals("customer_name_u", classifer.predict(null, new SimpleHeader("customername_u")));
        }
    }

    @Test
    public void testSimpleClassifierWithCamel() throws Exception {
        try (final var classifer = new SimpleTagClassifier(model, TagClassifier.TagStyle.CAMEL)) {
            assertEquals("customerNameu", classifer.predict(null, new SimpleHeader("customer name_u")));
            assertEquals("customerNameu", classifer.predict(null, new SimpleHeader("customer_name_u")));
            assertEquals("customerNameu", classifer.predict(null, new SimpleHeader("customerName_u")));
            assertEquals("customerNameu", classifer.predict(null, new SimpleHeader("customername_u")));
        }
    }

    @Test
    public void testSimpleClassifierCompatible() throws Exception {
        try (final var classifer = new SimpleTagClassifier(model, TagClassifier.TagStyle.NONE)) {
            assertEquals("customer_name", classifer.predict(null, new SimpleHeader("customer name")));
            assertEquals("customer_name", classifer.predict(null, new SimpleHeader("customer_name")));
            assertEquals("customerName", classifer.predict(null, new SimpleHeader("customername")));
        }
    }
}
