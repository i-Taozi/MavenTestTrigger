/**
 * 
 */
package com.github.jmkgreen.morphia.query;

import org.junit.Test;

import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;


public class TestMandatoryId extends TestBase {
	
	@Entity
	public static class E {
		// not id here
		String foo = "bar";
	}
	
	@Test
	public final void testMissingId() {
		new AssertedFailure() {
			
			@Override
			protected void thisMustFail() throws Throwable {
				morphia.map(E.class);
			}
		};
	}
	
	@Test
	public final void testMissingIdNoImplicitMapCall() {
		final Key<E> save = ds.save(new E());
		
		new AssertedFailure() {
			@Override
			protected void thisMustFail() throws Throwable {
				E byKey = ds.getByKey(E.class, save);
			}
		};
	}

}
