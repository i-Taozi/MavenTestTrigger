/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import java.util.ArrayList;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Property;
import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
@SuppressWarnings("unchecked")
public class RegisterAfterUseTest extends TestBase {
	
	public static class Broken extends TestEntity {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Property("foo")
		@Embedded("bar")
		ArrayList l;
	}
	
	@Test
	@Ignore(value = "not yet implemented")
	public void testRegisterAfterUse() throws Exception {
		
		// this would have failed: morphia.map(Broken.class);

		Broken b = new Broken();
		ds.save(b); // imho must not work
		Assert.fail();
		
		// doe not revalidate due to being used already!
		morphia.map(Broken.class);
		Assert.fail();
	}
}
