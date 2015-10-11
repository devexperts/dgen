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


import com.sun.source.doctree.*;
import com.sun.source.util.DocTreeScanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that presents Javadoc comment in usable format.
 */
@SuppressWarnings("Since15")
public class ParsedComment {

	private final String fullComment;
	private final String firstSentence;
	private final String firstParagraph;
	private final String returnTagValue;
	private final String dgenAnnotateTagValue;
	private final Map<String, String> params;

	private ParsedComment(String fullComment, String firstSentence, String firstParagraph,
		String returnTagValue, String dgenAnnotateTagValue, Map<String, String> params)
	{
		this.fullComment = fullComment;
		this.firstSentence = firstSentence;
		this.firstParagraph = firstParagraph;
		this.returnTagValue = returnTagValue;
		this.dgenAnnotateTagValue = dgenAnnotateTagValue;
		this.params = params;
	}

	/**
     * @param docComment {@link DocCommentTree comment node} to be parsed.
     * @return {@link ParsedComment parsed comment} from {@link DocCommentTree javac comment node}.
     */
	public static ParsedComment createFromDocComment(final DocCommentTree docComment) {
		if (docComment == null)
			return null;

		String fullComment = docComment.toString();
		String firstSentence = docComment.getFirstSentence().size() > 0 ? docComment.getFirstSentence().get(0).toString().trim() : null;

		final String[] annotatedTagValue = {null};
		final String[] returnTagValue = {null};
		final Map<String, String> params = new HashMap<>();
		final StringBuilder firstParagraphBuilder = new StringBuilder();

		docComment.accept(new DocTreeScanner<Void, Void>() {
			private static final String ANNOTATED_TAG = "dgen.annotate";
			private boolean inFirstParagraph = true;
			private int currentTextBlockNumber = 0;

			@Override
			public Void scan(DocTree docTree, Void aVoid) {
				if (!(docTree instanceof TextTree))
					inFirstParagraph = false;
				return super.scan(docTree, aVoid);
			}

			@Override
			public Void visitReturn(ReturnTree returnTree, Void aVoid) {
				returnTagValue[0] = returnTree.getDescription().toString();
				return super.visitReturn(returnTree, aVoid);
			}

			@Override
			public Void visitUnknownBlockTag(UnknownBlockTagTree tagTree, Void aVoid) {
				if (tagTree.getTagName().equals(ANNOTATED_TAG))
					annotatedTagValue[0] = tagTree.getContent().toString().trim();
				return super.visitUnknownBlockTag(tagTree, aVoid);
			}

			@Override
			public Void visitUnknownInlineTag(UnknownInlineTagTree tagTree, Void aVoid) {
				if (tagTree.getTagName().equals(ANNOTATED_TAG))
					annotatedTagValue[0] = tagTree.getContent().toString().trim();
				return super.visitUnknownInlineTag(tagTree, aVoid);
			}

			@Override
			public Void visitParam(ParamTree paramTree, Void aVoid) {
				params.put(paramTree.getName().toString(), paramTree.getDescription().toString().trim());
				return super.visitParam(paramTree, aVoid);
			}

			@Override
			public Void visitText(TextTree textTree, Void aVoid) {
				if (inFirstParagraph) {
					currentTextBlockNumber++;
					if (currentTextBlockNumber == 2) { // We should add space characters between first and second sentences
						String docCommentAfterFirstSentence = docComment.toString().substring(firstParagraphBuilder.length());
						int secondSentenceIndex = docCommentAfterFirstSentence.indexOf(textTree.toString());
						if (secondSentenceIndex > 0)
							firstParagraphBuilder.append(docCommentAfterFirstSentence.substring(0, secondSentenceIndex));
					}
					firstParagraphBuilder.append(textTree);
				}
				return super.visitText(textTree, aVoid);
			}
		}, null);

		String firstParagraph = firstParagraphBuilder.toString().trim();
		if (firstParagraph.isEmpty())
			firstParagraph = null;

		return new ParsedComment(fullComment, firstSentence, firstParagraph, returnTagValue[0], annotatedTagValue[0], params);
	}

	/**
	 * @return full comment or {@code null} if Javadoc isn't presented.
	 */
	public String getFullComment() {
		return fullComment;
	}

	/**
	 * @return first sentence or {@code null} if Javadoc is empty or contains only tags.
	 */
	public String getFirstSentence() {
		return firstSentence;
	}

	/**
	 * @return first paragraph or {@code null} if Javadoc is empty or contains only tags.
	 */
	public String getFirstParagraph() {
		return firstParagraph;
	}

	/**
	 * @return value in {@code return} tag ot {@code null} if Javadoc does not contain such tag.
	 */
	public String getReturnTagValue() {
		return returnTagValue;
	}

	/**
	 * @return last <code>@dgen.annotated</code> tag value or {@code null} if tag isn't presented.
	 */
	public String getDgenAnnotateTagValue() {
		return dgenAnnotateTagValue;
	}

	/**
	 * @return map with comments for @param tags. Map: paramName -&gt; paramDocComment.
	 */
	public Map<String, String> getParams() {
		return params;
	}
}
