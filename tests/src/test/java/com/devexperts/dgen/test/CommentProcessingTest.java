/*
 * #%L
 * Dgen - Description generator
 * %%
 * Copyright (C) 2015 - 2020 Devexperts, LLC
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.devexperts.dgen.test;

import org.junit.Test;

import java.lang.reflect.Method;

import static com.devexperts.dgen.test.Utils.assertEquals;
import static com.devexperts.dgen.test.Utils.getDescription;
import static org.junit.Assert.assertNull;

/**
 * Tests Javadoc processing.
 */
@SuppressWarnings("ALL")
public class CommentProcessingTest {

    /**
     *
     *    A
     *
     * @dgen.annotate
     */
    class A {}

    @Test
    public void descriptionValueShouldBeTrimmed() {
        assertEquals("A", getDescription(A.class));
    }

    /**
     * @dgen.annotate field {}
     */
    class B {

        /**
         * A. B.
         */
        int a;

        /**
         * B
         * <p/>
         * B
         */
        int b;

        /**
         * C
         */
        int c;

        int d;
    }

    @Test
    public void onlyFirstSentenceShouldBeInDescription() throws NoSuchFieldException {
        assertEquals("A.", getDescription(B.class.getDeclaredField("a")));
        assertEquals("B", getDescription(B.class.getDeclaredField("b")));
        assertEquals("C", getDescription(B.class.getDeclaredField("c")));
        assertNull("d has empty Javadoc", getDescription(B.class.getDeclaredField("d")));
    }

    class C {

        /**
         * @param a a
         * @param b b
         *          bbb
         *
         * @dgen.annotate
         */
        void f(int a, int b) {
        }

    }

    @Test
    public void allParamTagsShouldBeParsed() throws NoSuchMethodException {
        Method f = C.class.getDeclaredMethod("f", int.class, int.class);
        assertEquals("a", getDescription(f, 0).value());
        assertEquals("b\n          bbb", getDescription(f, 1).value());
    }

    /**
     * @dgen.annotate options { retrieveStrategy = firstParagraph; } field {}
     */
    class D {

        /**
         * A
         */
        int a;

        /**
         * A. B.
         *
         * C.
         * @tag
         * D.
         */
        int b;

        /**
         * A.<p/>B.
         */
        int c;

        /**
         * @tag
         */
        int d;

        int e;
    }

    @Test
    public void firstParagraphShouldBePresentedWithTheSameOption() throws NoSuchFieldException {
        assertEquals("A", getDescription(D.class.getDeclaredField("a")));
        assertEquals("A. B.\n\n C.", getDescription(D.class.getDeclaredField("b")));
        assertEquals("A.", getDescription(D.class.getDeclaredField("c")));
        assertNull("d has only tags in it's Javadoc", getDescription(D.class.getDeclaredField("d")));
        assertNull("e has empty Javadoc", getDescription(D.class.getDeclaredField("e")));
    }
}
