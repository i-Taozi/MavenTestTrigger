package com.github.jmkgreen.morphia.query;

import com.github.jmkgreen.morphia.TestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 29/05/2013
 * Time: 21:54
 * To change this template use File | Settings | File Templates.
 */
public class KnownFields extends TestBase {

    @Test
    public void testKnownFields() {
        Query<A> q = ds.createQuery(A.class);
        Assert.assertNotNull(q.retrieveKnownFields());

    }

    static class A {
        String foo;
        String bar;
    }
}
