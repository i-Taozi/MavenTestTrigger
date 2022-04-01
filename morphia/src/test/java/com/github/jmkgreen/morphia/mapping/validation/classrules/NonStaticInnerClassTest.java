/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * 
 */
public class NonStaticInnerClassTest extends TestBase {
	
	static class Valid {
		@Id
		ObjectId id;
	}
	
	class InValid {
		@Id
		ObjectId id;
	}
	
	@Test
	public void testValidInnerClass() throws Exception {
		morphia.map(Valid.class);
	}
	
	@Test
	public void testInValidInnerClass() throws Exception {
		new AssertedFailure(MappingException.class) {
			@Override
			protected void thisMustFail() throws Throwable {
				morphia.map(InValid.class);
			}
		};
	}
}
