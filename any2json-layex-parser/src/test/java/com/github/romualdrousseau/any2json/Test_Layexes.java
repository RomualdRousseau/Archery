package com.github.romualdrousseau.any2json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.StringLexer;


/**
 * Unit test for Any2Json.
 */
public class Test_Layexes {

    @Test
    public void testCompile() throws Exception {
        final var matcher = new Layex("(()(h+$))(()(v+$)())+(f+$)$").compile();
        final var expected = "CONCAT(GROUP(CONCAT(GROUP(NOP),GROUP(CONCAT(MANY(VALUE('h'), 1, 2147483647),EOR)))),CONCAT(GROUP(MANY(CONCAT(GROUP(NOP),CONCAT(GROUP(CONCAT(MANY(VALUE('v'), 1, 2147483647),EOR)),GROUP(NOP))), 1, 2147483647)),CONCAT(GROUP(CONCAT(MANY(VALUE('f'), 1, 2147483647),EOR)),EOR)))";
        assertEquals(expected, matcher.toString());
    }

    @Test
    public void testStringMatch() throws Exception {
        final var lexer = new StringLexer("hhhh$vvvv$vvvv$vvvv$ffff$$");
        final var matcher = new Layex("(()(h+$))(()(v+$)())+(f+$)$").compile();
        // CONCAT(GROUP(CONCAT(GROUP(NOP),GROUP(CONCAT(MANY(VALUE('h'), 1, 2147483647),EOR)))),CONCAT(GROUP(MANY(CONCAT(GROUP(NOP),CONCAT(GROUP(CONCAT(MANY(VALUE('v'), 1, 2147483647),EOR)),GROUP(NOP))), 1, 2147483647)),GROUP(CONCAT(MANY(VALUE('f'), 1, 2147483647),EOR))))
        assertTrue(matcher.match(lexer));
    }

    @Test
    public void testStringDontMatch() throws Exception {
        final var lexer = new StringLexer("hhhh$vvvv$vvvv$vsvv$ffff$$");
        final var matcher = new Layex("(()(h+$))(()(v+$)())+(f+$)$").compile();
        assertFalse(matcher.match(lexer));
    }
}
