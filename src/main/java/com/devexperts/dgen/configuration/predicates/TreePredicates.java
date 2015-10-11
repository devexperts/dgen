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


import com.devexperts.dgen.configuration.FormattingUtils;
import com.sun.tools.javac.tree.JCTree;

import java.util.List;

/**
 * Utility methods for {@link TreePredicate}.
 */
public class TreePredicates {

	private static final TreePredicate ALWAYS_TRUE = new TreePredicate() {
		@Override
		public boolean apply(JCTree tree) {
			return true;
		}

		@Override
		public String toString() {
			return "TRUE";
		}
	};

    // utility class
    private TreePredicates() {
    }

    /**
	 * @return a predicate that always evaluates to {@code true}.
	 */
	public static TreePredicate alwaysTrue() {
		return ALWAYS_TRUE;
	}

	/**
     * @param predicates predicates to be combined.
	 * @return a predicate that evaluates to {@code true} if ANY of its components evaluates to {@code true}.
	 * Evaluates to {@code false} if have no components.
	 */
	public static TreePredicate or(List<TreePredicate> predicates) {
		return new OrPredicate(predicates);
	}

	/**
     * @param predicates predicates to be combined.
	 * @return a predicate that evaluates to {@code true}if EACH of its components evaluates to {@code true}.
	 * Evaluates to {@code true} if have no components.
	 */
	public static TreePredicate and(List<TreePredicate> predicates) {
		return new AndPredicate(predicates);
	}

	private static final class OrPredicate extends ListPredicate {

		private OrPredicate(List<TreePredicate> predicates) {
			super(predicates, "OR");
		}

		@Override
		public boolean apply(JCTree tree) {
			for (TreePredicate predicate : predicates) {
				if (predicate.apply(tree))
					return true;
			}
			return false;
		}
	}

	private static final class AndPredicate extends ListPredicate {

		private AndPredicate(List<TreePredicate> predicates) {
			super(predicates, "AND");
		}

		@Override
		public boolean apply(JCTree tree) {
			for (TreePredicate predicate : predicates) {
				if (!predicate.apply(tree))
					return false;
			}
			return true;
		}
	}

	private abstract static class ListPredicate extends TreePredicate {

		protected final List<TreePredicate> predicates;
		protected final String name;

		protected ListPredicate(List<TreePredicate> predicates, String name) {
			this.predicates = predicates;
			this.name = name;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			builder.append(name).append(" {\n");
			for (TreePredicate predicate : predicates) {
				FormattingUtils.appendWithTabShift(builder, predicate);
				builder.append("\n");
			}
			builder.append("}");

			return builder.toString();
		}
	}
}
