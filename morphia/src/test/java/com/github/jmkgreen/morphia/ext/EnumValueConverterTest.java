package com.github.jmkgreen.morphia.ext;


import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Converters;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.converters.SimpleValueConverter;
import com.github.jmkgreen.morphia.converters.TypeConverter;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.mongodb.DBObject;
import org.junit.Assert;

/**
 * Example converter which stores the enum value instead of string (name)
 * @author scotthernandez
 */
public class EnumValueConverterTest extends TestBase {

	@SuppressWarnings({"rawtypes", "unused"})
	static private class AEnumConverter extends TypeConverter implements SimpleValueConverter{

		public AEnumConverter() { super(AEnum.class); }

		@Override
		public
		Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
			if (fromDBObject == null) return null;
			return AEnum.values()[(Integer) fromDBObject];
		}

		@Override
		public
		Object encode(Object value, MappedField optionalExtraInfo) {
			if (value == null)
				return null;

			return ((Enum) value).ordinal();
		}
	}

	private static enum AEnum {
		One,
		Two

	}

	@SuppressWarnings("unused")
	@Converters(AEnumConverter.class)
	private static class EnumEntity {
		@Id ObjectId id = new ObjectId();
		AEnum val = AEnum.Two;

	}

	@Test
	public void testEnum() {
		EnumEntity ee = new EnumEntity();
		ds.save(ee);
		DBObject dbObj = ds.getCollection(EnumEntity.class).findOne();
		Assert.assertEquals(1, dbObj.get("val"));
	}
}
