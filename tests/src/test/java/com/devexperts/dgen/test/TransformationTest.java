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

import com.devexperts.annotation.Description;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that all classes are transformed with expected side-effects.
 */
@SuppressWarnings("ALL")
public class TransformationTest {

    class A {

        @Deprecated
        int a;

        /**
         * B
         * @dgen.annotate
         */
        @Deprecated
        int b;
    }

    @Test
    public void nonProcessedFieldShouldHaveTheSameAnnotations() throws NoSuchFieldException {
        Field a = A.class.getDeclaredField("a");
        assertNotNull("a should be annotated with @Deprecated", a.getAnnotation(Deprecated.class));
        assertEquals("a should have 1 annotation", 1, a.getAnnotations().length);
    }

    @Test
    public void processedFieldShouldHaveDescriptionAnnotation() throws NoSuchFieldException {
        Field b = A.class.getDeclaredField("b");
        assertNotNull("a should be annotated with @Deprecated", b.getAnnotation(Deprecated.class));
        assertNotNull("a should be annotated with @Description", b.getAnnotation(Description.class));
        assertEquals("a should have 2 annotation", 2, b.getAnnotations().length);
    }

    /**
     * Complex class should be processed without errors.
     *
     * @dgen.annotate field {} method {}
     */
    public static class ComplexClass {
        /**Test*/ private int x;
        /**Test*/ public ComplexClass() { this(0); }
        /**Test*/ public ComplexClass(int x) { this.x = x; }
        /**Test*/ public ComplexClass(ComplexClass other) { this.x = other.x; }

        /**Test*/ public static void s1() { return; }
        /**Test*/ public static void s2() { s1(); return; }

        /**Test*/ public static void main(String[] args) { ComplexClass dc = new ComplexClass(); dc.overloadTester(); }

        /**Test*/ public void i1() {}
        /**Test*/ public void i2() { s1(); i1(); return; }

        /**Test*/ public void overloadTester() { System.out.println("Test"); overload((byte)1); overload((short)1); }

        /**Test*/ public void overload(byte b) { System.out.println("byte"); }
        /**Test*/ public void overload(short s) { System.out.println("short"); }
    }
}
