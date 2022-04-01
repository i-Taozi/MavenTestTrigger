/**
 * 
 */
package com.github.jmkgreen.morphia.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.TestBase;

/**
 * @author ScottHernandez
 */
public class LongIdEntityTest extends TestBase
{
	static class MyEntity extends LongIdEntity {
		protected MyEntity () {super(null);};
		public MyEntity (Datastore ds) {super(ds);}
	}
	
	@Test
	public void testMonoIncreasingId() throws Exception {
		MyEntity ent = new MyEntity(ds);
		ds.save(ent);
		assertEquals(1L, ent.myLongId, 0);
		ent = new MyEntity(ds);
		ds.save(ent);
		assertEquals(2L, ent.myLongId, 0);
	}

}
