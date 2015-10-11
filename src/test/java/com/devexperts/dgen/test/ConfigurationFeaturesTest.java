package com.devexperts.dgen.test;

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.devexperts.dgen.test.Utils.assertEquals;
import static com.devexperts.dgen.test.Utils.getDescription;
import static org.junit.Assert.assertNull;

/**
 * Tests configuration features.
 */
@SuppressWarnings("ALL")
public class ConfigurationFeaturesTest {

	/**A*/ class A {}

	// configuration in file
	@Test
	public void classFromConfigurationFileShouldBeAnnotated() {
		assertEquals("A", getDescription(A.class));
	}

	/** @dgen.annotate field { access = private; } */
	class B1 {
		/**A*/ private int a;       /**B*/ int b;
		/**C*/ protected int c;     /**D*/ public int d;
	}

	/** @dgen.annotate field { access = default; } */
	class B2 {
		/**A*/ private int a;       /**B*/ int b;
		/**C*/ protected int c;     /**D*/ public int d;
	}

	/** @dgen.annotate field { access = protected; } */
	class B3 {
		/**A*/ private int a;       /**B*/ int b;
		/**C*/ protected int c;     /**D*/ public int d;
	}

	/** @dgen.annotate field { access = public; } */
	class B4 {
		/**A*/ private int a;       /**B*/ int b;
		/**C*/ protected int c;     /**D*/ public int d;
	}

	@Test
	public void checkAccessModifierPredicateWorksAsWell() throws NoSuchFieldException {
		assertEquals("A", getDescription(B1.class.getDeclaredField("a")));
		assertNull("b shouldn't be processed", getDescription(B1.class.getDeclaredField("b")));
		assertNull("c shouldn't be processed", getDescription(B1.class.getDeclaredField("c")));
		assertNull("d shouldn't be processed", getDescription(B1.class.getDeclaredField("d")));

		assertNull("a shouldn't be processed", getDescription(B2.class.getDeclaredField("a")));
		assertEquals("B", getDescription(B2.class.getDeclaredField("b")));
		assertNull("c shouldn't be processed", getDescription(B2.class.getDeclaredField("c")));
		assertNull("d shouldn't be processed", getDescription(B2.class.getDeclaredField("d")));

		assertNull("a shouldn't be processed", getDescription(B3.class.getDeclaredField("a")));
		assertNull("b shouldn't be processed", getDescription(B3.class.getDeclaredField("b")));
		assertEquals("C", getDescription(B3.class.getDeclaredField("c")));
		assertNull("d shouldn't be processed", getDescription(B3.class.getDeclaredField("d")));

		assertNull("a shouldn't be processed", getDescription(B4.class.getDeclaredField("a")));
		assertNull("b shouldn't be processed", getDescription(B4.class.getDeclaredField("b")));
		assertNull("c shouldn't be processed", getDescription(B4.class.getDeclaredField("c")));
		assertEquals("D", getDescription(B4.class.getDeclaredField("d")));
	}

	/**
	 * @dgen.annotate field { isStatic = true; }
	 */
	static class C {
		/**A*/ int a;
		/**B*/ static int b;
	}

	@Test
	public void checkIsStaticPredicateWorksAsWell() throws NoSuchFieldException {
		assertNull("a shouldn't be processed", getDescription(C.class.getDeclaredField("a")));
		assertEquals("B", getDescription(C.class.getDeclaredField("b")));
	}

	/**
	 * @dgen.annotate field { name = "[ab]"; }
	 */
	class D {
		/**A*/ int a;
		/**B*/ int b;
		/**C*/ int c;
	}

	@Test
	public void checkNamePredicateWorksAsWell() throws NoSuchFieldException {
		assertEquals("A", getDescription(D.class.getDeclaredField("a")));
		assertEquals("B", getDescription(D.class.getDeclaredField("b")));
		assertNull("c shouldn't be processed", getDescription(D.class.getDeclaredField("c")));
	}

	/**E1*/ abstract class E1<T> implements Collection<T> {}
	/**E2*/ class E2<T> extends ArrayList<T> {}
	/**E3*/ abstract class E3<T> extends E1<T> {}

	// Configuration in file
	@Test
	public void checkInstanceOfPredicateWorksAsWell() {
		assertEquals("E1", getDescription(E1.class));
		assertEquals("E1", getDescription(E1.class));
		assertEquals("E1", getDescription(E1.class));
	}

	/**
	 * @dgen.annotate field { access = public; isStatic = true; }
	 */
	static class F {
		/**A*/ int a;
		/**B*/ static int b;
		/**C*/ public int c;
		/**D*/ public static int d;
	}

	@Test
	public void checkSeveralPredicatesInRuleWorksAsWell() throws NoSuchFieldException {
		assertNull("a shouldn't be processed", getDescription(F.class.getDeclaredField("a")));
		assertNull("b shouldn't be processed", getDescription(F.class.getDeclaredField("b")));
		assertNull("c shouldn't be processed", getDescription(F.class.getDeclaredField("c")));
		assertEquals("D", getDescription(F.class.getDeclaredField("d")));
	}

	/**
	 * GG
	 *
	 * @dgen.annotate field {}
	 */
	@Description("G")
	class G {
		/**AA*/ @Description("A") int a;
		/**B*/ int b;
		/**FF*/ @Description("F") void f() {}
	}

	// part of configuration in file
	@Test
	public void checkConfigurationPriorities() throws NoSuchFieldException, NoSuchMethodException {
		assertEquals("G", getDescription(G.class));
		assertEquals("A", getDescription(G.class.getDeclaredField("a")));
		assertEquals("B", getDescription(G.class.getDeclaredField("b")));
		assertEquals("F", getDescription(G.class.getDeclaredMethod("f")));
	}

	class H {
		/**A*/ int a;
	}

	// Configuration in file
	@Test
	public void firstSuitableClassRuleShouldBeChosen() throws NoSuchFieldException {
		assertNull("a shouldn't be processed", getDescription(H.class.getDeclaredField("a")));
	}

	/**
	 * I
	 *
	 * @dgen.annotate options { annotateClass = false; }
	 */
	class I {}

	@Test
	public void checkAnnotateClassOption() {
		assertNull("class I shouldn't be annotated because of annotateClass=false option", getDescription(I.class));
	}

	/**
	 * A. B.
	 *
	 * C.
	 * <p/>
	 * D.
	 *
	 * @return E
	 *
	 * @dgen.annotate options { retrieveStrategy = firstParagraph; }
	 */
	class J1 {}

	/**
	 * A. B.
	 *
	 * C.
	 * <p/>
	 * D.
	 *
	 * @return E.
	 *
	 * @dgen.annotate options { retrieveStrategy = returnTag; }
	 */
	class J2 {}

	/**
	 * A. B.
	 *
	 * C.
	 * <p/>
	 * D.
	 *
	 * @return E.
	 *
	 * @dgen.annotate options { retrieveStrategy = all; }
	 */
	class J3 {}

	@Test
	public void checkRetrieveStrategyOption() {
		assertEquals("A. B.\n\n C.", getDescription(J1.class));
		assertEquals("E.", getDescription(J2.class));
		assertEquals(
			"A. B.\n\n C.\n <p/>\n D.\n@return E.\n@dgen.annotate options { retrieveStrategy = all; }",
			getDescription(J3.class)
		);
	}
}
