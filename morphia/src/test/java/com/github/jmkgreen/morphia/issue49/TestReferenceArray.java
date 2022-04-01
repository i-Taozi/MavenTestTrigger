package com.github.jmkgreen.morphia.issue49;

import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.testutil.TestEntity;

public class TestReferenceArray extends TestBase
{

    @Test
    public final void testArrayPersistence()
    {
        A a = new A();
        B b1 = new B();
        B b2 = new B();

        a.bs[0] = b1;;
        a.bs[1] = b2;;

        ds.save(b2, b1, a);

        a = ds.get(a);
    }

   
    public static class A extends TestEntity
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Reference
        B[] bs = new B[2];
    }

    public static class B extends TestEntity
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String foo;
    }

}
