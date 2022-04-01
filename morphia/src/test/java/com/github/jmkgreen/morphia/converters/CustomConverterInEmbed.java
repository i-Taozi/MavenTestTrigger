/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.testutil.TestEntity;
import org.junit.Assert;

/**
 * @author Uwe Schaefer
 *
 */
@SuppressWarnings("rawtypes")
public class CustomConverterInEmbed extends TestBase {

	public static class E1 extends TestEntity {
		private static final long serialVersionUID = 1L;
		List<Foo> foo = new LinkedList<Foo>();
	}

	public static class E2 extends TestEntity {
		private static final long serialVersionUID = 1L;
		Map<String, Foo> foo = new HashMap<String, Foo>();
	}

	// unknown type to convert
	public static class Foo {
		private String string;

		Foo(){}
		public Foo(String string) {
			this.string = string;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	public static class FooConverter extends TypeConverter implements SimpleValueConverter  {

		public boolean done;

		public FooConverter() {
			super(Foo.class);
		}

		@Override
		public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo)
				throws MappingException {
			return new Foo((String) fromDBObject);
		}

		@Override
		public Object encode(Object value, MappedField optionalExtraInfo) {
			done = true;
			return value.toString();
		}

		public boolean didConversion() {
			return done;
		}
	}

	//FIXME issue 101

	@Test
	public void testConversionInList() throws Exception {
		FooConverter fc = new FooConverter();
		morphia.getMapper().getConverters().addConverter(fc);
		E1 e = new E1();
		e.foo.add(new Foo("bar"));
		ds.save(e);
		Assert.assertTrue(fc.didConversion());
	}

	@Test
	public void testConversionInMap() throws Exception {
		FooConverter fc = new FooConverter();
		morphia.getMapper().getConverters().addConverter(fc);
		E2 e = new E2();
		e.foo.put("bar",new Foo("bar"));
		ds.save(e);

		Assert.assertTrue(fc.didConversion());

		e = ds.find(E2.class).get();
		Assert.assertNotNull(e.foo);
		Assert.assertFalse(e.foo.isEmpty());
		Assert.assertTrue(e.foo.containsKey("bar"));
		Assert.assertEquals(e.foo.get("bar").string, "bar");
	}

}
