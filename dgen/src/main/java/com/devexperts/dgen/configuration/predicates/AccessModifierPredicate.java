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

import com.sun.tools.javac.tree.JCTree;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * Checks that tree's access modifier matches with ANY of specified.
 * <p>
 * Possible access modifiers:
 * {@link AccessModifier#PRIVATE},
 * {@link AccessModifier#DEFAULT},
 * {@link AccessModifier#PROTECTED},
 * {@link AccessModifier#PUBLIC}.
 * <p>
 * Can be applied to JCClassDecl|JCMethodDecl|JCVariableDecl.
 */
public class AccessModifierPredicate extends TreePredicate {

    private final Set<AccessModifier> accessModifiers;

    public AccessModifierPredicate(Collection<AccessModifier> accessModifiers) {
        this.accessModifiers = EnumSet.copyOf(accessModifiers);
    }

    @Override
    protected boolean apply(JCTree.JCClassDecl classDecl) {
        return apply(classDecl.getModifiers());
    }

    @Override
    protected boolean apply(JCTree.JCMethodDecl methodDecl) {
        return apply(methodDecl.getModifiers());
    }

    @Override
    protected boolean apply(JCTree.JCVariableDecl variableDecl) {
        return apply(variableDecl.getModifiers());
    }

    private boolean apply(JCTree.JCModifiers declModifiers) {
        for (AccessModifier modifier : accessModifiers) {
            if (modifier.apply(declModifiers.getFlags()))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("access = ");
        for (Iterator<AccessModifier> modifierIterator = accessModifiers.iterator(); modifierIterator.hasNext(); ) {
            builder.append(modifierIterator.next());
            if (modifierIterator.hasNext())
                builder.append("|");
        }
        builder.append(";");

        return builder.toString();
    }

    public enum AccessModifier {
        PRIVATE() {
            @Override
            public boolean apply(Set<Modifier> modifiers) {
                return modifiers.contains(Modifier.PRIVATE);
            }
        },

        DEFAULT() {
            @Override
            public boolean apply(Set<Modifier> modifiers) {
                return !modifiers.contains(Modifier.PRIVATE)
                    && !modifiers.contains(Modifier.PROTECTED)
                    && !modifiers.contains(Modifier.PUBLIC);
            }
        },

        PROTECTED() {
            @Override
            public boolean apply(Set<Modifier> modifiers) {
                return modifiers.contains(Modifier.PROTECTED);
            }
        },

        PUBLIC() {
            @Override
            public boolean apply(Set<Modifier> modifiers) {
                return modifiers.contains(Modifier.PUBLIC);
            }
        };

        public abstract boolean apply(Set<Modifier> modifiers);

    }
}
