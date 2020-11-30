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
package com.devexperts.dgen.configuration;

/**
 * Utility class for helping to format configuration string value.
 */
public class FormattingUtils {

    // Utility class
    private FormattingUtils() {
    }

    /**
     * Append object to specified {@code string builder} with tab shifts for each line.
     *
     * @param builder string builder.
     * @param o       object to be appended.
     * @return reference to specified {@code string builder}.
     */
    public static StringBuilder appendWithTabShift(StringBuilder builder, Object o) {
        builder.append("\t").append(o.toString().replace("\n", "\n\t"));
        return builder;
    }
}
