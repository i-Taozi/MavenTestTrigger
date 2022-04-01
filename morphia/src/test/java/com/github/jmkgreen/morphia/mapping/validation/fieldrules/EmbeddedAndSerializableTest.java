/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.fieldrules;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Serialized;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolationException;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;
import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class EmbeddedAndSerializableTest extends TestBase {
	public static class E extends TestEntity {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Embedded
		@Serialized
		R r;
	}
	
	public static class R {
	}
	
	@Test
	public void testCheck() {
		new AssertedFailure(ConstraintViolationException.class) {
			public void thisMustFail() throws Throwable {
				morphia.map(E.class);
			}
		};
	}
	
}
