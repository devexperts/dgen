package com.devexperts.dgen.configuration.predicates;

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


import com.sun.tools.javac.tree.JCTree;

import java.util.regex.Pattern;

/**
 * Checks that tree's name matches with specified regex.
 * Uses qualified name for classes and simple name for other elements.
 * <p>
 * Can be applied to JCClassDecl|JCMethodDecl|JCVariableDecl.
 */
public class NamePredicate extends TreePredicate {

	private final Pattern pattern;

	public NamePredicate(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	@Override
	protected boolean apply(JCTree.JCClassDecl classDecl) {
		classDecl.sym.getQualifiedName();
		return apply(classDecl.sym.getQualifiedName().toString());
	}

	@Override
	protected boolean apply(JCTree.JCMethodDecl methodDecl) {
		return apply(methodDecl.getName().toString());
	}

	@Override
	protected boolean apply(JCTree.JCVariableDecl variableDecl) {
		return apply(variableDecl.getName().toString());
	}

	private boolean apply(String name) {
		return pattern.matcher(name).matches();
	}

	@Override
	public String toString() {
		return "name = \"" + pattern.pattern() + "\";";
	}
}
