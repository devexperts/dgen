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
import org.junit.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Utility class for reading descriptions in tests.
 */
public class Utils {

    // Utility class
    private Utils() {
    }

    /**
     * Returns description for element.
     *
     * @param element annotated element.
     * @return value from {@link com.devexperts.annotation.Description} annotation for specified element
     * or {@code null} if element isn't annotated with {@link com.devexperts.annotation.Description}.
     */
    public static String getDescription(AnnotatedElement element) {
        Description description = element.getAnnotation(Description.class);
        return description != null ? description.value() : null;
    }

    /**
     * Returns description for method parameter.
     *
     * @param method      method.
     * @param paramNumber number of argument in {@code method}.
     * @return value from {@link com.devexperts.annotation.Description} annotation for specified method parameter.
     * or {@code null} if parameter isn't annotated with {@link com.devexperts.annotation.Description}
     */
    public static Description getDescription(Method method, int paramNumber) {
        Annotation[] paramAnnotations = method.getParameterAnnotations()[paramNumber];
        for (Annotation annotation : paramAnnotations) {
            if (annotation instanceof Description)
                return (Description)annotation;
        }

        return null;
    }

    /**
     * Like {@link org.junit.Assert#assertEquals}, but replaces {@code \n} character with {@code \}
     * for more clear test failure message.
     */
    public static void assertEquals(String expected, String actual) {
        Assert.assertEquals(
                expected != null ? expected.replace("\n", "\\n") : null,
                actual != null ? actual.replace("\n", "\\n") : null
        );
    }
}
