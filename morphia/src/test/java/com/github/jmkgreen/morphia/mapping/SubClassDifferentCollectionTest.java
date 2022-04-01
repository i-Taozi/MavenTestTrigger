/*
 * Copyright 2012 jamesgreen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Index;
import com.github.jmkgreen.morphia.annotations.Indexes;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jamesgreen
 */
public class SubClassDifferentCollectionTest {
    @Entity("Base")
    @Indexes(@Index("id"))
    private static class Foo {
    	@Id int id;
    }

    @Entity("Sub")
    @Indexes(@Index("test"))
    private static class Foobie extends Foo {
    	String test;
    }

    @Entity()
    private static class Fooble extends Foobie {

    }

    @Test
    public void testSubEntity() {
        MappedClass foobieMapped = new MappedClass(Foobie.class, new DefaultMapper());
        Entity entity = foobieMapped.getEntityAnnotation();
        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.value());
        System.out.println("Foobie value: " + entity.value() + ", collection name: " + foobieMapped.getCollectionName());
        Assert.assertTrue("Sub".equals(entity.value()));
    }

    @Test
    public void testEmptyEntity() {
        MappedClass foobleMapped = new MappedClass(Fooble.class, new DefaultMapper());
        Entity entity = foobleMapped.getEntityAnnotation();
        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.value());
        System.out.println("Fooble value: " + entity.value() + ", collection name: " + foobleMapped.getCollectionName());
        Assert.assertTrue(Mapper.IGNORED_FIELDNAME.equals(entity.value()));
        Assert.assertTrue(Fooble.class.getSimpleName().equals(foobleMapped.getCollectionName()));
    }
}
