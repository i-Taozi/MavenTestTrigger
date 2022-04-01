package com.github.jmkgreen.morphia.mapping.lazy;

import java.util.Iterator;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.testutil.AssertedFailure;
import com.github.jmkgreen.morphia.testutil.TestEntity;

@SuppressWarnings("unused")
public class LazyWithMissingReferent extends TestBase {
	
	static class E {
		@Id
		ObjectId id = new ObjectId();
		@Reference
		E2 e2;
	}
	
	static class ELazy {
		@Id
		ObjectId id = new ObjectId();
		@Reference(lazy = true)
		E2 e2;
	}
	
	static class ELazyIgnoreMissing {
		@Id
		ObjectId id = new ObjectId();
		@Reference(lazy = true, ignoreMissing = true)
		E2 e2;
	}
	
	static class E2 extends TestEntity {
		@Id
		ObjectId id = new ObjectId();
		String foo = "bar";
		
		void foo() {
		};
	}
	
	@Test
	public void testMissingRef() throws Exception {
		final E e = new E();
		E2 e2 = new E2();
		e.e2 = e2;
		
		ds.save(e); // does not fail due to preinited Ids

		new AssertedFailure(MappingException.class) {
			@Override
			protected void thisMustFail() throws Throwable {
				ds.createQuery(E.class).asList();
			}
		};
	}

	@Test
	public void testMissingRefLazy() throws Exception {
		final ELazy e = new ELazy();
		E2 e2 = new E2();
		e.e2 = e2;
		
		ds.save(e); // does not fail due to preinited Ids
		
		new AssertedFailure(MappingException.class) {
			@Override
			protected void thisMustFail() throws Throwable {
				ds.createQuery(ELazy.class).asList();
			}
		};
	}
	
	@Test
	public void testMissingRefLazyIgnoreMissing() throws Exception {
		final ELazyIgnoreMissing e = new ELazyIgnoreMissing();
		E2 e2 = new E2();
		e.e2 = e2;
		
		ds.save(e); // does not fail due to preinited Ids
		Iterator<ELazyIgnoreMissing> i = ds.createQuery(ELazyIgnoreMissing.class).iterator();
		final ELazyIgnoreMissing x = i.next();

		new AssertedFailure() {
			@Override
			protected void thisMustFail() throws Throwable {
				// reference must be resolved for this
				x.e2.foo();
			}
		};
	}
}
