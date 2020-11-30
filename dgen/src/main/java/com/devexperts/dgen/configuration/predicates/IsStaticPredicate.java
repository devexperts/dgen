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
package com.devexperts.dgen.configuration.predicates;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

/**
 * Checks that tree has specified static modifier.
 * <p>
 * Can be applied to JCClassDecl|JCMethodDecl|JCVariableDecl.
 */
public class IsStaticPredicate extends TreePredicate {

    private final boolean isStatic;

    public IsStaticPredicate(boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    protected boolean apply(JCTree.JCClassDecl classDecl) {
        return apply(classDecl.sym);
    }

    @Override
    protected boolean apply(JCTree.JCMethodDecl methodDecl) {
        return apply(methodDecl.sym);
    }

    @Override
    protected boolean apply(JCTree.JCVariableDecl variableDecl) {
        return apply(variableDecl.sym);
    }

    private boolean apply(Symbol symbol) {
        return Flags.isStatic(symbol) == isStatic;
    }

    @Override
    public String toString() {
        return "isStatic = " + isStatic + ";";
    }
}
