/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.gedcom4j.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Unit test for {@link Soundex}
 * 
 * @author frizbog
 */
public class SoundexTest {

    /**
     * Test that the constructor is private
     * 
     * @throws NoSuchMethodException
     *             if the method can't be found
     * @throws IllegalAccessException
     *             if the method cannot be accessed
     * @throws InvocationTargetException
     *             the reflected target throws an exception
     * @throws InstantiationException
     *             if the class can't be instantiated
     */
    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        Class<Soundex> clazz = Soundex.class;
        assertTrue("class must be final", Modifier.isFinal(clazz.getModifiers()));
        assertEquals("There must be only one constructor", 1, clazz.getDeclaredConstructors().length);
        final Constructor<Soundex> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers())) {
            fail("constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(clazz)) {
                fail("there exists a non-static method:" + method);
            }
        }
    }

    /**
     * Negative test case for {@link Soundex#soundex(String)}
     */
    @Test
    public void testNegative() {
        assertNull(Soundex.soundex(null));
        assertNull(Soundex.soundex("12345"));
        assertNull(Soundex.soundex("!@#$%"));
        assertNull(Soundex.soundex("\r\n\t\r\n"));
    }

    /**
     * Positive test cases for {@link Soundex#soundex(String)}
     */
    @Test
    public void testPositiveExtraNonAlphacharacters() {
        assertEquals("B000", Soundex.soundex("!bbbb!"));
        assertEquals("B000", Soundex.soundex("{bbbb}"));
        assertEquals("B000", Soundex.soundex(" { bbbb } "));
        assertEquals("A161", Soundex.soundex("Ab er fo rth"));
        assertEquals("A100", Soundex.soundex("A b e"));
        assertEquals("B100", Soundex.soundex("Bu bba"));
        assertEquals("B316", Soundex.soundex("Bed f o rd"));
        assertEquals("C642", Soundex.soundex("Ch 12arles"));
        assertEquals("D230", Soundex.soundex("Da g125wood"));
        assertEquals("E242", Soundex.soundex("Egg!!leston"));
        assertEquals("F200", Soundex.soundex("Fa4x1$io"));
        assertEquals("G553", Soundex.soundex("Ga12!@#\nnym\4ede"));
    }

    /**
     * Positive test cases for {@link Soundex#soundex(String)}
     */
    @Test
    public void testPositiveRoutine() {
        assertEquals("A161", Soundex.soundex("Aberforth"));
        assertEquals("A100", Soundex.soundex("Abe"));
        assertEquals("B100", Soundex.soundex("Bubba"));
        assertEquals("B316", Soundex.soundex("Bedford"));
        assertEquals("C642", Soundex.soundex("Charles"));
        assertEquals("D230", Soundex.soundex("Dagwood"));
        assertEquals("E242", Soundex.soundex("Eggleston"));
        assertEquals("F200", Soundex.soundex("Faxio"));
        assertEquals("G553", Soundex.soundex("Ganymede"));
        assertEquals("O416", Soundex.soundex("Oliver"));
        assertEquals("X160", Soundex.soundex("Xavier"));
        assertEquals("Z162", Soundex.soundex("Zebracake"));
    }

}
