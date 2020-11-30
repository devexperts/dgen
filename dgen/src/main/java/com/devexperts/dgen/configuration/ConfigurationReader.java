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

import com.devexperts.dgen.configuration.predicates.AccessModifierPredicate;
import com.devexperts.dgen.configuration.predicates.ExtendsOrImplementsPredicate;
import com.devexperts.dgen.configuration.predicates.IsStaticPredicate;
import com.devexperts.dgen.configuration.predicates.NamePredicate;
import com.devexperts.dgen.configuration.predicates.TreePredicate;
import com.devexperts.dgen.configuration.predicates.TreePredicates;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for reading configuration.
 */
public final class ConfigurationReader {

    // No constructor, utility class
    private ConfigurationReader() {
    }

    /**
     * Read configuration from specified file.
     *
     * @param filename file which contains configuration.
     * @return read configuration.
     * @throws java.io.IOException           if any problems occurred during file reading.
     * @throws IllegalStateException if any problems occurred during configuration parsing.
     */
    public static Configuration readConfigurationFromFile(String filename) throws IOException {
        DgenConfigurationLexer lexer = new DgenConfigurationLexer(new ANTLRFileStream(filename));
        DgenConfigurationParser parser = new DgenConfigurationParser(new CommonTokenStream(lexer));

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws IllegalStateException
            {
                throw new IllegalStateException("Failed to parse configuration at " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });

        final List<ClassRule> classRules = new ArrayList<>();
        parser.addParseListener(new DgenConfigurationBaseListener() {
            @Override
            public void exitClassRule(@NotNull DgenConfigurationParser.ClassRuleContext ctx) {
                classRules.add(parseClassRule(ctx));
            }
        });
        parser.fileConfiguration();

        return new Configuration(classRules);
    }

    /**
     * Create {@link ClassRule class rule} from comment.
     * Rule should contains only @code{options}, @{code method} and {@code field} rules.
     *
     * @param comment comment with class rule configuration.
     * @return {@code class rule} which was created from comment.
     */
    public static ClassRule readClassRuleFromComment(final String comment) {
        DgenConfigurationLexer lexer = new DgenConfigurationLexer(new ANTLRInputStream(comment));
        DgenConfigurationParser parser = new DgenConfigurationParser(new CommonTokenStream(lexer));

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws IllegalStateException
            {
                throw new IllegalStateException("Failed to parse configuration at " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });

        final List<MethodRule> methodRules = new ArrayList<>();
        final List<FieldRule> fieldRules = new ArrayList<>();
        final ClassRuleOptions[] options = {ClassRuleOptions.EMPTY};

        parser.addParseListener(new DgenConfigurationBaseListener() {
            @Override
            public void exitClassCommentConfiguration(DgenConfigurationParser.ClassCommentConfigurationContext ctx) {
                methodRules.addAll(ctx.methodRule().stream()
                        .map(ConfigurationReader::parseMethodRule).collect(Collectors.toList()));

                fieldRules.addAll(ctx.fieldRule().stream()
                        .map(ConfigurationReader::parseFieldRule).collect(Collectors.toList()));

                if (ctx.classRuleOptions().size() > 1)
                    throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());
                if (!ctx.classRuleOptions().isEmpty())
                    options[0] = parseClassRuleOptions(ctx.classRuleOptions().get(0));
            }
        });
        parser.classCommentConfiguration();

        return new ClassRule(TreePredicates.alwaysTrue(), options[0], methodRules, fieldRules);
    }

    /**
     * @param comment comment with method rule configuration.
     * @return  {@code method rule} which was created from comment.
     */
    public static MethodRule readMethodRuleFromComment(String comment) {
        DgenConfigurationLexer lexer = new DgenConfigurationLexer(new ANTLRInputStream(comment));
        DgenConfigurationParser parser = new DgenConfigurationParser(new CommonTokenStream(lexer));

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws IllegalStateException
            {
                throw new IllegalStateException("Failed to parse configuration at " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });

        final MethodRuleOptions[] methodRuleOptions = {MethodRuleOptions.EMPTY};
        parser.addParseListener(new DgenConfigurationBaseListener() {
            @Override
            public void exitMethodCommentConfiguration(DgenConfigurationParser.MethodCommentConfigurationContext ctx) {
                if (ctx.methodRuleOptions().size() > 1)
                    throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());

                if (!ctx.methodRuleOptions().isEmpty())
                    methodRuleOptions[0] = parseMethodRuleOptions(ctx.methodRuleOptions().get(0));
            }
        });
        parser.methodCommentConfiguration();

        return new MethodRule(TreePredicates.alwaysTrue(), methodRuleOptions[0]);
    }

    /**
     * @param comment comment with field rule configuration.
     * @return {@code field rule} which was created from comment.
     */
    public static FieldRule readFieldRuleFromComment(String comment) {
        DgenConfigurationLexer lexer = new DgenConfigurationLexer(new ANTLRInputStream(comment));
        DgenConfigurationParser parser = new DgenConfigurationParser(new CommonTokenStream(lexer));

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws IllegalStateException
            {
                throw new IllegalStateException("Failed to parse configuration at " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });

        final FieldRuleOptions[] fieldRuleOptions = {FieldRuleOptions.EMPTY};
        parser.addParseListener(new DgenConfigurationBaseListener() {
            @Override
            public void exitFieldCommentConfiguration(DgenConfigurationParser.FieldCommentConfigurationContext ctx) {
                if (ctx.fieldRuleOptions().size() > 1)
                    throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());

                if (!ctx.fieldRuleOptions().isEmpty())
                    fieldRuleOptions[0] = parseFieldRuleOptions(ctx.fieldRuleOptions().get(0));
            }
        });
        parser.methodCommentConfiguration();

        return new FieldRule(TreePredicates.alwaysTrue(), fieldRuleOptions[0]);
    }

    private static MethodRuleOptions parseMethodRuleOptions(DgenConfigurationParser.MethodRuleOptionsContext ctx) {
        DescriptionRetrieveStrategy retrieveStrategy = null;
        if (ctx.descriptionRetrieveStrategy().size() > 1)
            throw new IllegalStateException("Two or more same configurations: " + ctx.getText());
        if (!ctx.descriptionRetrieveStrategy().isEmpty())
            retrieveStrategy =  parseDescriptionRetrieveStrategy(ctx.descriptionRetrieveStrategy().get(0));

        return new MethodRuleOptions(retrieveStrategy);
    }

    private static FieldRuleOptions parseFieldRuleOptions(DgenConfigurationParser.FieldRuleOptionsContext ctx) {
        DescriptionRetrieveStrategy retrieveStrategy = null;
        if (ctx.descriptionRetrieveStrategy().size() > 1)
            throw new IllegalStateException("Two or more same configurations: " + ctx.getText());
        if (!ctx.descriptionRetrieveStrategy().isEmpty())
            retrieveStrategy =  parseDescriptionRetrieveStrategy(ctx.descriptionRetrieveStrategy().get(0));

        return new FieldRuleOptions(retrieveStrategy);
    }

    private static ClassRuleOptions parseClassRuleOptions(DgenConfigurationParser.ClassRuleOptionsContext ctx) {
        DescriptionRetrieveStrategy retrieveStrategy = null;
        if (ctx.descriptionRetrieveStrategy().size() > 1)
            throw new IllegalStateException("Two or more same configurations: " + ctx.getText());
        if (!ctx.descriptionRetrieveStrategy().isEmpty())
            retrieveStrategy =  parseDescriptionRetrieveStrategy(ctx.descriptionRetrieveStrategy().get(0));

        boolean annotateClass = true;
        if (ctx.annotateClass().size() > 1)
            throw new IllegalStateException("Two or more same configurations: " + ctx.getText());

        if (!ctx.annotateClass().isEmpty() && ctx.annotateClass().get(0).FALSE() != null)
            annotateClass = false;

        return new ClassRuleOptions(retrieveStrategy, annotateClass);
    }

    private static DescriptionRetrieveStrategy parseDescriptionRetrieveStrategy(DgenConfigurationParser.DescriptionRetrieveStrategyContext ctx) {
        if (ctx.firstSentenceStrategy() != null)
            return DescriptionRetrieveStrategy.FIRST_SENTENCE;
        if (ctx.firstParagraphStrategy() != null)
            return DescriptionRetrieveStrategy.FIRST_PARAGRAPH;
        if (ctx.returnTagStrategy() != null)
            return DescriptionRetrieveStrategy.RETURN_TAG_VALUE;
        if (ctx.allStrategy() != null)
            return DescriptionRetrieveStrategy.ALL;

        throw new IllegalStateException("Unknown strategy: " + ctx.getText());
    }

    private static ClassRule parseClassRule(DgenConfigurationParser.ClassRuleContext ctx) {
        List<TreePredicate> predicates = ctx.predicate().stream()
                .map(ConfigurationReader::parsePredicate).collect(Collectors.toList());

        List<MethodRule> methodRules = ctx.methodRule().stream()
                .map(ConfigurationReader::parseMethodRule).collect(Collectors.toList());

        List<FieldRule> fieldRules = ctx.fieldRule().stream()
                .map(ConfigurationReader::parseFieldRule).collect(Collectors.toList());

        ClassRuleOptions options = ClassRuleOptions.EMPTY;
        if (ctx.classRuleOptions().size() > 1)
            throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());
        if (!ctx.classRuleOptions().isEmpty())
            options = parseClassRuleOptions(ctx.classRuleOptions().get(0));

        return new ClassRule(TreePredicates.and(predicates), options, methodRules, fieldRules);
    }

    private static MethodRule parseMethodRule(DgenConfigurationParser.MethodRuleContext ctx) {
        List<TreePredicate> predicates = ctx.predicate()
                .stream().map(ConfigurationReader::parsePredicate).collect(Collectors.toList());

        MethodRuleOptions options = MethodRuleOptions.EMPTY;
        if (ctx.methodRuleOptions().size() > 1)
            throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());
        if (!ctx.methodRuleOptions().isEmpty())
            options = parseMethodRuleOptions(ctx.methodRuleOptions().get(0));

        return new MethodRule(TreePredicates.and(predicates), options);
    }

    private static FieldRule parseFieldRule(DgenConfigurationParser.FieldRuleContext ctx) {
        List<TreePredicate> predicates = ctx.predicate()
                .stream().map(ConfigurationReader::parsePredicate).collect(Collectors.toList());

        FieldRuleOptions options = FieldRuleOptions.EMPTY;
        if (ctx.fieldRuleOptions().size() > 1)
            throw new IllegalStateException("Two or more options blocks are founded:\n" + ctx.getText());
        if (!ctx.fieldRuleOptions().isEmpty())
            options = parseFieldRuleOptions(ctx.fieldRuleOptions().get(0));

        return new FieldRule(TreePredicates.and(predicates), options);
    }

    private static TreePredicate parsePredicate(DgenConfigurationParser.PredicateContext ctx) {
        if (ctx.namePredicate() != null)
            return parseNamePredicate(ctx.namePredicate());

        if (ctx.isStaticPredicate() != null)
            return parseIsStaticPredicate(ctx.isStaticPredicate());

        if (ctx.accessModifierPredicate() != null)
            return parseAccessModifierPredicate(ctx.accessModifierPredicate());

        if (ctx.extendsOrImplementsPredicate() != null)
            return parseInstanceOfPredicate(ctx.extendsOrImplementsPredicate());

        throw new IllegalStateException("Unknown predicate: " + ctx.getText());
    }

    private static TreePredicate parseAccessModifierPredicate(
        DgenConfigurationParser.AccessModifierPredicateContext ctx)
    {
        List<AccessModifierPredicate.AccessModifier> modifiers = new ArrayList<>();
        for (DgenConfigurationParser.AccessModifierValueContext modifierContext : ctx.accessModifierValue()) {
            AccessModifierPredicate.AccessModifier modifier;
            if (modifierContext.PRIVATE() != null) {
                modifier = AccessModifierPredicate.AccessModifier.PRIVATE;
            } else if (modifierContext.DEFAULT() != null) {
                modifier = AccessModifierPredicate.AccessModifier.DEFAULT;
            } else if (modifierContext.PROTECTED() != null) {
                modifier = AccessModifierPredicate.AccessModifier.PROTECTED;
            } else if (modifierContext.PUBLIC() != null) {
                modifier = AccessModifierPredicate.AccessModifier.PUBLIC;
            } else {
                throw new IllegalStateException("Unknown access modifier: " + modifierContext.getText());
            }
            modifiers.add(modifier);
        }
        return new AccessModifierPredicate(modifiers);
    }

    private static TreePredicate parseIsStaticPredicate(DgenConfigurationParser.IsStaticPredicateContext ctx) {
        boolean isStaticValue = ctx.TRUE() != null;
        return new IsStaticPredicate(isStaticValue);
    }

    private static TreePredicate parseNamePredicate(DgenConfigurationParser.NamePredicateContext ctx) {
        String nameWithQuotes = ctx.name().getText();
        return new NamePredicate(nameWithQuotes.substring(1, nameWithQuotes.length() - 1));
    }

    private static TreePredicate parseInstanceOfPredicate(DgenConfigurationParser.ExtendsOrImplementsPredicateContext ctx) {
        String nameWithQuotes = ctx.name().getText();
        return new ExtendsOrImplementsPredicate(nameWithQuotes.substring(1, nameWithQuotes.length() - 1));
    }
}
