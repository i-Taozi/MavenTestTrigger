/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.fieldrules;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolationException;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;
import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class LazyReferenceOnArrayTest extends TestBase {
	
	public static class LazyOnArray extends TestEntity {
		private static final long serialVersionUID = 1L;
		@Reference(lazy = true)
		R[] r;
	}
	
	public static class R extends TestEntity {
		private static final long serialVersionUID = 1L;
	}
	
	@Test
	public void testLazyRefOnArray() {
		new AssertedFailure(ConstraintViolationException.class) {
			
			@Override
			protected void thisMustFail() throws Throwable {
				morphia.map(LazyOnArray.class);
			}
		};
	}
}
