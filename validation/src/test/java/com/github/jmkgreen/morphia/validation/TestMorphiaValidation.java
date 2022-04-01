/**
 * 
 */
package com.github.jmkgreen.morphia.validation;

import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Email;
import org.junit.Test;

import com.github.jmkgreen.morphia.annotations.Id;

/**
 * @author doc
 */
public class TestMorphiaValidation extends TestBase
{

    public static class E
    {
        @Id
        ObjectId id;
        @Email
        String email;
    }

    /**
     * Test method for
     * {@link com.github.jmkgreen.morphia.validation.MorphiaValidationExtension#prePersist(java.lang.Object, com.mongodb.DBObject, com.github.jmkgreen.morphia.mapping.Mapper)}
     * .
     */
    @Test
    public final void testPrePersist()
    {
        final E e = new E();
        e.email = "not an email";

        new ValidationExtension(this.morphia);

        new AssertedFailure()
        {

            @Override
            protected void thisMustFail() throws Throwable
            {
                TestMorphiaValidation.this.ds.save(e);
            }
        };

        e.email = "foo@bar.com";
        this.ds.save(e);

    }

}
