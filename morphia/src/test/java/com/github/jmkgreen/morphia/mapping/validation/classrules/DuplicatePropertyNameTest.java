/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import java.util.Map;



import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Property;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolationException;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class DuplicatePropertyNameTest extends TestBase {
	@Entity
	public static class DuplicatedPropertyName {
		@Id
		String id;
		
		@Property(value = "value")
		String content1;
		@Property(value = "value")
		String content2;
	}
	
	@Entity
	public static class DuplicatedPropertyName2 {
		@Id
		String id;

		@Embedded(value = "value")
		Map<String, Integer> content1;
		@Property(value = "value")
		String content2;
	}
	
	@Entity
	public static class Super {
		String foo;
	}
	
	public static class Extends extends Super {
		String foo;
	}
	
	
	@Test
	public void testDuplicatedPropertyName() throws Exception {
		new AssertedFailure(ConstraintViolationException.class) {
			public void thisMustFail() throws Throwable {
				morphia.map(DuplicatedPropertyName.class);
			}
		};
		new AssertedFailure(ConstraintViolationException.class) {
			public void thisMustFail() throws Throwable {
				morphia.map(DuplicatedPropertyName2.class);
			}
		};
		new AssertedFailure(ConstraintViolationException.class) {
			public void thisMustFail() throws Throwable {
				morphia.map(Extends.class);
			}
		};
	}
	
}
