/**
 * 
 */
package com.github.jmkgreen.morphia.callbacks;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import com.github.jmkgreen.morphia.AbstractEntityInterceptor;
import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.mongodb.DBObject;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 *
 */
public class TestEntityInterceptorMoment extends TestBase {
	
	static class E {
		@Id
		private ObjectId _id = new ObjectId();
		
		boolean called = false;
		
		@PrePersist
		void entityCallback() {
			called = true;
		}
	}
	
	public static class Interceptor extends AbstractEntityInterceptor {
		
		public void PostLoad(Object ent, DBObject dbObj, Mapper mapr) {
			// TODO Auto-generated method stub
			
		}
		
		public void PostPersist(Object ent, DBObject dbObj, Mapper mapr) {
			// TODO Auto-generated method stub
			
		}
		
		public void PreLoad(Object ent, DBObject dbObj, Mapper mapr) {
			// TODO Auto-generated method stub
			
		}
		
		public void PrePersist(Object ent, DBObject dbObj, Mapper mapr) {
			Assert.assertTrue(((E) ent).called);
		}
		
		public void PreSave(Object ent, DBObject dbObj, Mapper mapr) {
			// TODO Auto-generated method stub
			
		}

	}
	
	@Test
	public void testGlobalEntityInterceptorWorksAfterEntityCallback() {
		morphia.map(E.class);
		morphia.getMapper().addInterceptor(new Interceptor());
		
		ds.save(new E());
	}
}
