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


import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

/**
 * Checks that class extends (or implements) specified class (or interface) in declaration.
 * <p>
 * For example, if class {@code A} extends {@code java.util.ArrayList} then for class {@code A}
 * this predicate, parameterized with {@code java.util.ArrayList} will return {@code true},
 * but parameterized with {@code java.util.List} will return {@code false}.
 * <p>
 * Can be applied to JCClassDecl only.
 */
public class ExtendsOrImplementsPredicate extends TreePredicate {

	private final String className;

	public ExtendsOrImplementsPredicate(String className) {
		this.className = className;
	}

	@Override
	protected boolean apply(JCTree.JCClassDecl classDecl) {
		return apply0(classDecl);
	}

	private boolean apply0(JCTree.JCClassDecl classDecl) {
		if (checkClassName((Type.ClassType)classDecl.sym.type))
			return true;

		if (classDecl.getExtendsClause() != null && checkClassName((Type.ClassType)classDecl.getExtendsClause().type))
			return true;

		for (JCTree.JCExpression interfaceExpression : classDecl.getImplementsClause()) {
			if (checkClassName((Type.ClassType)interfaceExpression.type))
				return true;
		}

		return false;
	}

	private boolean checkClassName(Type.ClassType classType) {
		return className.equals(classType.tsym.getQualifiedName().toString());
	}
}
