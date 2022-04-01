package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.query.Query;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 26/04/2013
 * Time: 07:17
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class TestThreeMillion extends TestBase {
    private long id = 100000L;

    @Entity
    public static class Person {
        @Id
        private String id;

        private String fName, lName, f1, f2, f3;
        private Date birth;

    }

    @Before
    public void before() {
        map();
    }

    @Test
    public void testThreeMillionReturned() {
//        for (int m = 0; m < 3; m++) {
//            for (int i = 0; i < 1000000; i++) {
//                ads.insert(create());
//            }
//            System.out.println("Completed 1m inserts");
//        }

        for (int r=0; r < 10; r++) {
            read();
        }
    }

    protected Person create() {
        Person p = new Person();
        p.id = "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnm" + id;
        p.fName = "Foo";
        p.lName = "Bar";
        p.birth = new Date();
        p.f1 = "qwertyuiopasdfghjklzxcvbnm";
        p.f2 = "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnm";
        p.f3 = "qwertyuiopasdfghjklzxcvbnm";
        id++;
        return p;
    }

    protected void read() {
        Query<Person> q = ads.find(Person.class);
        int i = 0;
        for (Person p : q) {
            Assert.assertEquals("qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnm", p.f2);
            i++;
        }
        System.out.println("Read " + i + " documents");
    }

    protected void map() {
        this.morphia.map(Person.class);
    }
}