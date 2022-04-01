package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Entity;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;


/**
 * Test a simple entity with a javax.persistence.id
 */
public class JavaxPersistIdTest extends TestBase {

    @Entity
    private static class Person {
        @Id
        ObjectId id;

        String value;
    }

    @Test
    public void testPersonPersist() {
        Person p = new Person();
        p.value = "something";
        morphia.map(Person.class);
        Key<Person> key = ds.save(p);
        Person persisted = ds.find(Person.class).get();
        Assert.assertNotNull(persisted.id);
        Assert.assertTrue(persisted.id instanceof ObjectId);
        Assert.assertEquals("something", persisted.value);
    }

    @Test
    public void testPersonMapped() {
        MappedClass mc = new MappedClass(Person.class, new DefaultMapper());
        MappedField mf = mc.getMappedField("_id");
        Assert.assertNotNull(mf);
        Assert.assertTrue(mf.hasAnnotation(Id.class));
        Field field = mc.getIdField();
        Assert.assertNotNull(field);
    }

    @Test
    public void testFoundId() {
        MappedClass mc = new MappedClass(Person.class, new DefaultMapper());
        List<MappedField> fields = mc.getFieldsAnnotatedWith(Id.class);
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        MappedField mf = fields.get(0);
        Assert.assertEquals("id", mf.getJavaFieldName());
    }
}
