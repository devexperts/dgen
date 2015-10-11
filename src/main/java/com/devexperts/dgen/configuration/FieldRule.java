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

/**
 * Field rule contains predicates and options.
 */
public class FieldRule {

	private final TreePredicate predicate;
	private final FieldRuleOptions options;

	public FieldRule(TreePredicate predicate, FieldRuleOptions options) {
		this.predicate = predicate;
		this.options = options;
	}

	public TreePredicate getPredicate() {
		return predicate;
	}

	public FieldRuleOptions getOptions() {
		return options;
	}
}
