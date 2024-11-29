package com.github.romualdrousseau.archery.commons.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

public class Test_Tensor {

    @Test
    @Tag("unit")
    public void testNull() {
        final Tensor M1 = Tensor.Null;
        final Tensor M2 = Tensor.zeros(1);
        assertEquals(M1, Tensor.Null);
        assertNotEquals(M2, Tensor.Null);
    }

    @Test
    @Tag("unit")
    public void testZero() {
        final Tensor M1 = Tensor.zeros(4);
        final Tensor M2 = Tensor.of(0, 0, 0, 0);
        final Tensor M3 = Tensor.of(1, 1, 1, 1);
        assertThat(M1, equalsTo(M2, 0.0f));
        assertThat(M1, not(equalsTo(M3, 0.0f)));
    }

    @Test
    @Tag("unit")
    public void testEquals() {
        final Tensor M1 = Tensor.of(1, 2, 3, 4, 5);
        final Tensor M2 = Tensor.of(1, 2, 3, 4, 5);
        final Tensor M3 = Tensor.of(2, 3, 4, 5, 6);
        final Tensor M4 = Tensor.of(2, 3, 4);
        assertThat(M1, equalsTo(M2, 0.0f));
        assertThat(M1, not(equalsTo(M3, 0.0f)));
        assertThat(M1, not(equalsTo(M4, 0.0f)));
    }

    @Test
    @Tag("unit")
    public void testIAdd() {
        final Tensor M1 = Tensor.of(1, 2, 3, 4, 5);
        final Tensor M2 = Tensor.of(2, 4, 6, 8, 10);
        M1.iadd(Tensor.of(1, 2, 3, 4, 5));
        assertThat(M1, equalsTo(M2, 0.0f));
    }

    @Test
    @Tag("unit")
    public void testIf_lt_then() {
        final Tensor M1 = Tensor.of(1, 2, 3, 4, 5);
        final Tensor M2 = Tensor.of(0, 0, 1, 1, 1);
        M1.if_lt_then(3, 0, 1);
        assertThat(M1, equalsTo(M2, 0.0f));
    }

    @Test
    @Tag("unit")
    public void testArgMax() {
        final Tensor M1 = Tensor.Null;
        final Tensor M2 = Tensor.of(1, 2, 5, 4, 3);
        assertEquals(-1, M1.argmax());
        assertEquals(2, M2.argmax());
    }

    private static Matcher<Tensor> equalsTo(final Tensor expectedTensor, final float epsilon) {
        return new BaseMatcher<Tensor>() {
            @Override
            public boolean matches(final Object item) {
                return ((Tensor) item).equals(expectedTensor, epsilon);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText(expectedTensor.toString());
            }
        };
    }
}
