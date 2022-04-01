/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.fieldrules;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Serialized;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;
import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
@SuppressWarnings("unchecked")
public class MapNotSerializableTest extends TestBase {
	public static class Map1 extends TestEntity {
 		private static final long serialVersionUID = 1L;
		@Serialized
		Map<Integer, String> shouldBeOk = new HashMap();
		
	}
	
	public static class Map2 extends TestEntity {
		private static final long serialVersionUID = 1L;
		@Reference
		Map<Integer, E1> shouldBeOk = new HashMap();
		
	}
	
	public static class Map3 extends TestEntity {
		private static final long serialVersionUID = 1L;
		@Embedded
		Map<E2, Integer> shouldBeOk = new HashMap();
		
	}
	
	public static class E1 {
		
	}
	
	public static class E2 {
		
	}
	
	@Test
	public void testCheck() {
		morphia.map(Map1.class);
		
		new AssertedFailure() {
			public void thisMustFail() throws Throwable {
				morphia.map(Map2.class);
			}
		};
		
		new AssertedFailure() {
			public void thisMustFail() throws Throwable {
				morphia.map(Map3.class);
			}
		};
	}
	
}
