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


/**
 * Options for {@link MethodRule method rule}.
 */
public class MethodRuleOptions {

	public static final MethodRuleOptions EMPTY = new MethodRuleOptions(null);

	private final DescriptionRetrieveStrategy descriptionRetrieveStrategy;

	public MethodRuleOptions(DescriptionRetrieveStrategy descriptionRetrieveStrategy) {
		this.descriptionRetrieveStrategy = descriptionRetrieveStrategy;
	}

	public DescriptionRetrieveStrategy getDescriptionRetrieveStrategy() {
		return descriptionRetrieveStrategy;
	}
}
