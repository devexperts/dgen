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
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Determines {@code true} of {@code false} for given element.
 * Predicate can be applied to custom element kinds ONLY.
 * <p>
 * Can be applied to {@link com.sun.tools.javac.tree.JCTree.JCClassDecl}, {@link com.sun.tools.javac.tree.JCTree.JCMethodDecl} or {@link com.sun.tools.javac.tree.JCTree.JCVariableDecl}.
 */
public abstract class TreePredicate {

    /**
     * Returns the result of applying predicate to {@code tree}.
     * It's execution does not cause any observable side-effects.
     * It's consistent with equals, e.g. <code>Objects.equals(a, b) =&gt; predicate(a) == predicate(b)</code>.
     *
     * @param tree input.
     * @return the result of applying predicate to current tree.
     * @throws IllegalArgumentException if predicate cannot applied to current tree.
     */
    public boolean apply(JCTree tree) {
        if (tree instanceof JCClassDecl) {
            return apply((JCClassDecl) tree);
        } else if (tree instanceof JCMethodDecl) {
            return apply((JCMethodDecl) tree);
        } else if (tree instanceof JCVariableDecl) {
            return apply((JCVariableDecl) tree);
        } else {
            throw new IllegalArgumentException("Unsupported tree type, should be JCClassDecl|JCMethodDecl|JCVariableDecl: " + tree.getClass());
        }
    }

    protected boolean apply(JCClassDecl classDecl) {
        throw new IllegalStateException("Predicate does not support JCClassDecl input");
    }

    protected boolean apply(JCMethodDecl methodDecl) {
        throw new IllegalStateException("Predicate does not support JCMethodDecl input");
    }

    protected boolean apply(JCVariableDecl variableDecl) {
        throw new IllegalStateException("Predicate does not support JCVariableDecl input");
    }
}
