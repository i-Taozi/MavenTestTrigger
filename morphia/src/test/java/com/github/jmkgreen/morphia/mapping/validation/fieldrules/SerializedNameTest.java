/**
 * 
 */
package com.github.jmkgreen.morphia.mapping.validation.fieldrules;

import org.junit.Assert;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.PreSave;
import com.github.jmkgreen.morphia.annotations.Serialized;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.github.jmkgreen.morphia.testutil.TestEntity;
import com.mongodb.DBObject;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class SerializedNameTest extends TestBase {
	public static class E extends TestEntity {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Serialized("changedName")
		byte[] b = "foo".getBytes();
		
		@PreSave
		public void preSave(DBObject o) {
			document = o.toString();
//			System.out.println(document);
		}
		
		@Transient
		String document;
	}
	
	@Test
	public void testCheck() {
		
		E e = new E();
		ds.save(e);
		
		Assert.assertTrue(e.document.contains("changedName"));
	}
}
