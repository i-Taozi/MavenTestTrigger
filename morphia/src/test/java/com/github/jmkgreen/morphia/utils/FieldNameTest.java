/**
 *
 */
package com.github.jmkgreen.morphia.utils;

import org.junit.Test;
import org.junit.Assert;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;

public class FieldNameTest {

	private String foo;
	private String bar;

	@Test
	public void testFieldNameOf() throws Exception {
		String name = "foo";
		Assert.assertTrue("foo".equals(FieldName.of("foo")));
		Assert.assertTrue("bar".equals(FieldName.of("bar")));
		new AssertedFailure(FieldName.FieldNameNotFoundException.class) {

			@Override
			protected void thisMustFail() throws Throwable {
				FieldName.of("buh");
			}
		};
		Assert.assertTrue("x".equals(FieldName.of(E2.class, "x")));
		Assert.assertTrue("y".equals(FieldName.of(E2.class, "y")));
	}
}

class E1 {
	private final int x = 0;
}

class E2 extends E1 {
	private final int y = 0;
}
