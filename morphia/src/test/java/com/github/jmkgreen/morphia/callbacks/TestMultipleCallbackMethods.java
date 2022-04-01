package com.github.jmkgreen.morphia.callbacks;

import org.junit.Assert;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.PrePersist;

public class TestMultipleCallbackMethods extends TestBase {
	static abstract class CallbackAbstractEntity {
		@Id
		private String _id = new ObjectId().toStringMongod();
		
		public String getId() {
			return _id;
		}
		
		int foo = 0;
		
		@PrePersist
		void prePersist1() {
			foo++;
		}
		
		@PrePersist
		void prePersist2() {
			foo++;
		}
	}
	
	static class SomeEntity extends CallbackAbstractEntity {
		
	}
	
	@Test
	public void testMultipleCallbackAnnotation() throws Exception {
		SomeEntity entity = new SomeEntity();
		ds.save(entity);
		Assert.assertEquals(2, entity.foo);
	}
}