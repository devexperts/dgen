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


import com.devexperts.dgen.configuration.predicates.TreePredicate;
import com.sun.tools.javac.tree.JCTree;

import java.util.List;

/**
 * Class rule contains options, predicate for class, rules for fields and methods.
 */
public class ClassRule {

	private final TreePredicate classPredicate;
	private final ClassRuleOptions options;
	private final List<MethodRule> methodRules;
	private final List<FieldRule> fieldRules;

	public ClassRule(TreePredicate classPredicate, ClassRuleOptions options,
		List<MethodRule> methodRules, List<FieldRule> fieldRules)
	{
		this.classPredicate = classPredicate;
		this.options = options;
		this.methodRules = methodRules;
		this.fieldRules = fieldRules;
	}

	public MethodRule applyMethod(JCTree.JCMethodDecl methodDecl) {
		for (MethodRule methodRule : methodRules) {
			if (methodRule.getPredicate().apply(methodDecl))
				return methodRule;
		}

		return null;
	}

	public FieldRule applyField(JCTree.JCVariableDecl fieldDecl) {
		for (FieldRule fieldRule : fieldRules) {
			if (fieldRule.getPredicate().apply(fieldDecl))
				return fieldRule;
		}

		return null;
	}

	public TreePredicate getClassPredicate() {
		return classPredicate;
	}

	public ClassRuleOptions getOptions() {
		return options;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("class {\n");

		FormattingUtils.appendWithTabShift(builder, classPredicate);
		builder.append("\n");

		if (options != null) {
			FormattingUtils.appendWithTabShift(builder, options);
			builder.append("\n");
		}

		for (MethodRule methodRule : methodRules) {
			FormattingUtils.appendWithTabShift(builder, methodRule);
			builder.append("\n");
		}

		for (FieldRule fieldRule : fieldRules) {
			FormattingUtils.appendWithTabShift(builder, fieldRule);
			builder.append("\n");
		}

		builder.append("}");
		return builder.toString();
	}
}
