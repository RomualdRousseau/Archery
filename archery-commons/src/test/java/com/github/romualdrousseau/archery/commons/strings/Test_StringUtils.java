package com.github.romualdrousseau.archery.commons.strings;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.github.romualdrousseau.archery.commons.preprocessing.tokenizer.ShingleTokenizer;

public class Test_StringUtils {

    @Test
    @Tag("unit")
    public void testSnakeWithLemmatization() {
        final var tokenizer = new ShingleTokenizer(List.of("al", "total,tot", "dollar", "percent"));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("total quantity $", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("TotalQuantity$", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("totalquantity$", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("tot quantity $", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("TotQuantity$", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("totquantity$", tokenizer));
    }

    @Test
    @Tag("unit")
    public void testSnakeWithoutLemmatization() {
        final var tokenizer = new ShingleTokenizer(List.of("al", "total,tot", "dollar", "percent"), 1, false);
        assertEquals("total_quantity_dollar", StringUtils.toSnake("total quantity $", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("TotalQuantity$", tokenizer));
        assertEquals("total_quantity_dollar", StringUtils.toSnake("totalquantity$", tokenizer));
        assertEquals("tot_quantity_dollar", StringUtils.toSnake("tot quantity $", tokenizer));
        assertEquals("tot_quantity_dollar", StringUtils.toSnake("TotQuantity$", tokenizer));
        assertEquals("tot_quantity_dollar", StringUtils.toSnake("totquantity$", tokenizer));
    }

    @Test
    @Tag("unit")
    public void testCamelWithLemmatization() {
        final var tokenizer = new ShingleTokenizer(List.of("al", "total,tot", "dollar", "percent"));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("total quantity $", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("TotalQuantity$", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("totalquantity$", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("tot quantity $", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("TotQuantity$", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("totquantity$", tokenizer));
    }

    @Test
    @Tag("unit")
    public void testCamelWithoutLemmatization() {
        final var tokenizer = new ShingleTokenizer(List.of("al", "total,tot", "dollar", "percent"), 1, false);
        assertEquals("totQuantityDollar", StringUtils.toCamel("tot quantity $", tokenizer));
        assertEquals("totQuantityDollar", StringUtils.toCamel("TotQuantity$", tokenizer));
        assertEquals("totQuantityDollar", StringUtils.toCamel("totquantity$", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("total quantity $", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("TotalQuantity$", tokenizer));
        assertEquals("totalQuantityDollar", StringUtils.toCamel("totalquantity$", tokenizer));
    }

    @Test
    @Tag("unit")
    public void testCleanToken() {
        assertEquals("total quantity $", StringUtils.cleanToken("  total   quantity $  "));
        assertEquals("total quantity $", StringUtils.cleanToken(" \"\"\"  total   quantity $  \"\"\""));
    }

    @Test
    @Tag("unit")
    public void testMerge() {
        assertEquals("aaabbbccc", StringUtils.merge(" ", List.of("aaabbbccc", "aaa")).get());
        assertEquals("aaabbbccc ddd", StringUtils.merge(" ", List.of("aaabbbccc", "ddd")).get());
        assertEquals("aaabbbccc", StringUtils.merge(" ", List.of("aaa", "aaabbbccc")).get());
        assertEquals("aaabbbccc ddd", StringUtils.merge(" ", List.of("ddd", "aaabbbccc")).get());
    }
}
