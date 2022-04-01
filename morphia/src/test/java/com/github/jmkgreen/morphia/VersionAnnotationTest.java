/**
 * Copyright (C) 2010 Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jmkgreen.morphia;

import java.util.ConcurrentModificationException;

import com.github.jmkgreen.morphia.annotations.Entity;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Version;

/**
 * @author Scott Hernandez
 */

public class VersionAnnotationTest extends TestBase {

    @Entity
    private static class B {
        @Id
        ObjectId id = new ObjectId();
        @Version
        long version;
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testVersion() throws Exception {

        B b1 = new B();
        b1.version = 1;
        ds.save(b1);

        B b2 = new B();
        b2.id = b1.id;
        b2.version = 1;
        ds.save(b2);
    }

    @Test
    public void testVersionSuccess() {
        B b1 = new B();
        ds.save(b1);

        ds.save(b1);
        Assert.assertEquals(2, b1.version);
    }
}