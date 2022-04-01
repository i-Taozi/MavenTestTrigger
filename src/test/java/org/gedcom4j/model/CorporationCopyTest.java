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
package org.gedcom4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

/**
 * Test copy constructor for {@link Corporation}
 * 
 * @author frizbog
 */
public class CorporationCopyTest extends AbstractCopyTest {

    /**
     * Test copying a null {@link Corporation}, which should never work
     */
    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testCopyNull() {
        new Corporation(null);
    }

    /**
     * Test the simplest possible scenario - copy a new default {@link Corporation}
     */
    @Test
    public void testSimplestPossible() {
        Corporation orig = new Corporation();
        Corporation copy = new Corporation(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
    }

    /**
     * Test with values
     */
    @Test
    public void testWithValues() {
        Corporation orig = new Corporation();
        Address a = new Address();
        a.setAddr1("ZZZ");
        a.setCity("YYY");
        a.setStateProvince("ABC");
        a.setCountry("WWW");
        orig.setAddress(a);
        orig.setBusinessName("Bob's Genalogy Shop");
        orig.getCustomFacts(true).add(getTestCustomFact());
        orig.getNoteStructures(true).add(getTestNoteStructure());
        orig.getFaxNumbers(true).add(new StringWithCustomFacts("555-1212"));
        orig.getPhoneNumbers(true).add(new StringWithCustomFacts("555-1313"));
        orig.getWwwUrls(true).add(new StringWithCustomFacts("www.nowhere.com"));
        orig.getEmails(true).add(new StringWithCustomFacts("nobody@nowwhere.com"));

        Corporation copy = new Corporation(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
        assertEquals(orig.toString(), copy.toString());
    }

}
