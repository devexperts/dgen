package com.devexperts.dgen;

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


import com.devexperts.annotation.Description;
import com.devexperts.dgen.configuration.*;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

/**
 * This annotation processor generates {@link com.devexperts.annotation.Description} annotation
 * for classes/methods/fields from Javadoc.
 *
 * See README.md for details.
 */
@SuppressWarnings("Since15")
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DgenProcessor extends AbstractProcessor {

	private static final String DGEN_CONFIGURATION_PATH_KEY = "dgen.config";
	private static final String DGEN_CONFIGURATION_PATH_DEFAULT = "dgen.config";

	private Trees trees;
	private TreeMaker treeMaker;
	private Names names;
	private DocTrees docTrees;

	private Configuration configuration = new Configuration(Collections.<ClassRule>emptyList());

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		try {
			String filename = processingEnv.getOptions().get(DGEN_CONFIGURATION_PATH_KEY);
			if (filename == null) {
				if (Files.exists(Paths.get(DGEN_CONFIGURATION_PATH_DEFAULT))) {
					filename = DGEN_CONFIGURATION_PATH_DEFAULT;
				} else {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
						"Dgen configuration file not found, use empty configuration");
				}
			}
			if (filename != null) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING
					, "Load dgen configuration from " + filename);
				try {
					configuration = ConfigurationReader.readConfigurationFromFile(filename);
					processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING
						, "Dgen configuration loaded successfully:\n" + configuration);
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
						"Unable to load dgen configuration from \"" + filename + "\"");
				} catch (IllegalStateException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
						"Error during parsing configuration from \"" + filename + "\"");
				}
			}

			Context context = ((JavacProcessingEnvironment)processingEnv).getContext();

			trees = Trees.instance(processingEnv);
			treeMaker = TreeMaker.instance(context);
			names = Names.instance(context);
			docTrees = DocTrees.instance(processingEnv);
		} catch (Exception e) {
			logException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			Set<? extends Element> elements = roundEnv.getRootElements();
			for (Element element : elements) {
				switch (element.getKind()) {
				case CLASS:
				case INTERFACE:
				case ANNOTATION_TYPE:
				case ENUM:
					JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl)trees.getTree(element);
					CompilationUnitTree compilationUnitTree = trees.getPath(element).getCompilationUnit();
					classDecl.accept(new DescriptionGenerator(compilationUnitTree));
				}
			}
		} catch (Exception e) {
			logException(e);
		}

		return false;
	}

	private void logException(Exception e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
			"Exception occurred during dgen work: " + e.getClass().getName() + " " + e.getMessage());
		for (StackTraceElement stackTraceElement : e.getStackTrace()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				stackTraceElement.toString());
		}
	}

	private class DescriptionGenerator extends TreeTranslator {

		private final CompilationUnitTree compilationUnitTree;
		// classConfiguration.peek() contains current class configuration
		private final Stack<ClassRule> classRule = new Stack<>();
		private boolean visitClassMembers = false;

		private DescriptionGenerator(CompilationUnitTree compilationUnitTree) {
			this.compilationUnitTree = compilationUnitTree;
		}

		@Override
		public void visitClassDef(JCTree.JCClassDecl classDecl) {
			if (classDecl.sym == null) {
				super.visitClassDef(classDecl);
				return;
			}

			ParsedComment comment = ParsedComment.createFromDocComment(
				docTrees.getDocCommentTree(TreePath.getPath(compilationUnitTree, classDecl))
			);
			if (comment != null && comment.getDgenAnnotateTagValue() != null) {
				classRule.push(ConfigurationReader.readClassRuleFromComment(comment.getDgenAnnotateTagValue()));
			} else {
				classRule.push(configuration.applyClass(classDecl));
			}

			boolean currentVisitClassMembers = this.visitClassMembers;
			visitClassMembers = true;
			super.visitClassDef(classDecl);
			visitClassMembers = currentVisitClassMembers;

			if (classRule.peek() != null && classRule.peek().getOptions().isAnnotateClass()
				&& !containsDescriptionAnnotation(classDecl.getModifiers()) && comment != null)
			{
				DescriptionRetrieveStrategy retrieveStrategy = classRule.peek().getOptions().getDescriptionRetrieveStrategy();
				if (retrieveStrategy == null) // Use FIRST_SENTENCE strategy by default
					retrieveStrategy = DescriptionRetrieveStrategy.FIRST_SENTENCE;
				String description = getDescription(comment, retrieveStrategy);
				if (description != null)
					appendDescriptionAnnotation(classDecl.mods, description);
			}

			classRule.pop();
		}

		@Override
		public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
			boolean currentInsideClass = this.visitClassMembers;
			this.visitClassMembers = false;
			super.visitMethodDef(methodDecl);
			this.visitClassMembers = currentInsideClass;

			ParsedComment comment = ParsedComment.createFromDocComment(
				docTrees.getDocCommentTree(TreePath.getPath(compilationUnitTree, methodDecl))
			);

			boolean configuredInComment = false;
			MethodRule methodRule = null;
			if (comment != null && comment.getDgenAnnotateTagValue() != null) {
				methodRule = ConfigurationReader.readMethodRuleFromComment(comment.getDgenAnnotateTagValue());
				configuredInComment = true;
			} else if (visitClassMembers && classRule.peek() != null) {
				methodRule = classRule.peek().applyMethod(methodDecl);
			}

			if (methodRule != null && comment != null) {
				DescriptionRetrieveStrategy retrieveStrategy = methodRule.getOptions().getDescriptionRetrieveStrategy();
				if (retrieveStrategy == null && !configuredInComment && classRule.peek() != null) // get from class rule
					retrieveStrategy = classRule.peek().getOptions().getDescriptionRetrieveStrategy();
				if (retrieveStrategy == null) // use FIRST_SENTENCE by default
					retrieveStrategy = DescriptionRetrieveStrategy.FIRST_SENTENCE;

				// add description to method
				String methodDescription = getDescription(comment, retrieveStrategy);
				if (methodDescription != null && !containsDescriptionAnnotation(methodDecl.getModifiers()))
					appendDescriptionAnnotation(methodDecl.mods, methodDescription);

				// add description to params
                methodDecl.params.stream()
                        .filter(param -> !containsDescriptionAnnotation(param.getModifiers()))
                        .forEach(param -> {
                            String paramDescription = comment.getParams().get(param.getName().toString());
                            param.mods = appendDescriptionAnnotation(
                                    param.mods, param.getName().toString(),
                                    paramDescription != null ? paramDescription : ""
                            );
                        });
            }
		}

		@Override
		public void visitVarDef(JCTree.JCVariableDecl varDecl) {
			boolean currentInsideClass = this.visitClassMembers;
			this.visitClassMembers = false;
			super.visitVarDef(varDecl);
			this.visitClassMembers = currentInsideClass;

			ParsedComment comment = ParsedComment.createFromDocComment(
				docTrees.getDocCommentTree(TreePath.getPath(compilationUnitTree, varDecl))
			);

			boolean configuredInComment = false;
			FieldRule fieldRule = null;
			if (comment != null && comment.getDgenAnnotateTagValue() != null) {
				fieldRule = ConfigurationReader.readFieldRuleFromComment(comment.getDgenAnnotateTagValue());
				configuredInComment = true;
			} else if (visitClassMembers && classRule.peek() != null) {
				fieldRule = classRule.peek().applyField(varDecl);
			}

			if (fieldRule != null && comment != null && !containsDescriptionAnnotation(varDecl.getModifiers())) {
				DescriptionRetrieveStrategy retrieveStrategy = fieldRule.getOptions().getDescriptionRetrieveStrategy();
				if (retrieveStrategy == null && !configuredInComment && classRule.peek() != null) // get from class rule
					retrieveStrategy = classRule.peek().getOptions().getDescriptionRetrieveStrategy();
				if (retrieveStrategy == null) // use FIRST_SENTENCE by default
					retrieveStrategy = DescriptionRetrieveStrategy.FIRST_SENTENCE;

				String description = getDescription(comment, retrieveStrategy);
				if (description != null)
					appendDescriptionAnnotation(varDecl.mods, description);
			}
		}

		private boolean containsDescriptionAnnotation(JCTree.JCModifiers modifiers) {
			for (JCTree.JCAnnotation annotation : modifiers.getAnnotations()) {
				if (((Type.ClassType)annotation.type).tsym.getQualifiedName().toString().equals(Description.class.getCanonicalName()))
					return true;
			}
			return false;
		}

		/**
		 * Appends description annotation to specified modifiers.
		 */
		private JCTree.JCModifiers appendDescriptionAnnotation(JCTree.JCModifiers current, String name,
			String description)
		{
			List<JCTree.JCExpression> arguments = List.<JCTree.JCExpression>of(
                    treeMaker.Assign(treeMaker.Ident(names.fromString("name")), treeMaker.Literal(name)),
                    treeMaker.Assign(treeMaker.Ident(names.fromString("value")), treeMaker.Literal(description))
            );

			JCTree.JCAnnotation descriptionAnnotation = treeMaker.Annotation(
				createExpression(Description.class.getCanonicalName()),
				arguments
			);

			List<JCTree.JCAnnotation> annotations = current.getAnnotations().append(descriptionAnnotation);
			return treeMaker.Modifiers(current.flags, annotations);
		}

		/**
		 * Appends description annotation to specified modifiers.
		 */
		private void appendDescriptionAnnotation(JCTree.JCModifiers current, String description) {
			List<JCTree.JCExpression> arguments = List.<JCTree.JCExpression>of(
                    treeMaker.Literal(description)
            );
			JCTree.JCAnnotation descriptionAnnotation = treeMaker.Annotation(
				createExpression(Description.class.getCanonicalName()),
				arguments
			);

            current.annotations = current.getAnnotations().append(descriptionAnnotation);
        }

		/**
		 * Creates {@link com.sun.tools.javac.tree.JCTree.JCExpression} from {@code qualified name}.
		 */
		public JCTree.JCExpression createExpression(String qualifiedName) {
			String[] expressionParts = qualifiedName.split("\\.");

			JCTree.JCExpression expression = treeMaker.Ident(names.fromString(expressionParts[0]));
			for (int i = 1; i < expressionParts.length; i++) {
				expression = treeMaker.Select(expression, names.fromString(expressionParts[i]));
			}
			return expression;
		}

		/**
		 * Returns description depends on current class configuration.
		 *
		 * @throws NullPointerException if current class configuration does not exists.
		 */
		private String getDescription(ParsedComment comment, DescriptionRetrieveStrategy retrieveStrategy) {
			if (classRule.peek() == null)
				return comment.getFirstSentence();

			return retrieveStrategy.retrieveDescription(comment);
		}
	}
}
