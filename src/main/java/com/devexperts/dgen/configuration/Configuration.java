package com.devexperts.dgen.configuration;

/*
 * #%L
 * Dgen - Description generator
 * %%
 * Copyright (C) 2015 Devexperts, LLC
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


import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import java.util.List;

/**
 * Describes configuration file for dgen.
 * <p>
 * Contains list of class rules which should be used in natural order.
 */
public class Configuration {

	private final List<ClassRule> classRules;

	public Configuration(List<ClassRule> classRules) {
		this.classRules = classRules;
	}

    /**
     * @param classDecl {@code class declaration} to be applied.
     * @return first {@code class rule} which applied specified {@code class declaration}
     * or {@code null} if no configurations applied {@code class declaration}.
     * @throws IllegalArgumentException if class predicate in executed class configuration does not supported {@link com.sun.tools.javac.tree.JCTree.JCClassDecl}.
     */
    public ClassRule applyClass(JCClassDecl classDecl) {
		for (ClassRule classRule : classRules) {
			if (classRule.getClassPredicate().apply(classDecl))
				return classRule;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Configuration {\n");
		for (ClassRule classRule : classRules) {
			FormattingUtils.appendWithTabShift(builder, classRule);
			builder.append("\n");
		}
		builder.append("}");

		return builder.toString();
	}
}
