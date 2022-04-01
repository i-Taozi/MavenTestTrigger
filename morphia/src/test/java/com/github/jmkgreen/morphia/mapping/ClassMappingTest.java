/**
 *
 */
package com.github.jmkgreen.morphia.mapping;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Property;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class ClassMappingTest extends TestBase
{

    @SuppressWarnings("unchecked")
    public static class E {
		@Id ObjectId id;

        @Property Class<? extends Collection> testClass;
        Class<? extends Collection> testClass2;
    }

    @Test
    public void testMapping() throws Exception
    {
        E e = new E();

        e.testClass = LinkedList.class;
        ds.save(e);

        e = ds.get(e);
        Assert.assertEquals(LinkedList.class, e.testClass);
    }

    @Test
    public void testMappingWithoutAnnotation() throws Exception
    {
        E e = new E();

        e.testClass2 = LinkedList.class;
        ds.save(e);

        e = ds.get(e);
        Assert.assertEquals(LinkedList.class, e.testClass2);
    }

}
