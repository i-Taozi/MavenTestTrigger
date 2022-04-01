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

import java.io.IOException;

import org.gedcom4j.exception.GedcomParserException;
import org.junit.Test;

/**
 * Test copy constructor for {@link Submitter}
 * 
 * @author frizbog
 */
public class SubmitterCopyTest extends AbstractCopyTest {

    /**
     * Test copying a null {@link Submitter}, which should never work
     */
    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testCopyNull() {
        new Submitter(null);
    }

    /**
     * Test the simplest possible scenario - copy a new default {@link Submitter}
     */
    @Test
    public void testSimplestPossible() {
        Submitter orig = new Submitter();
        Submitter copy = new Submitter(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
    }

    /**
     * Test with a loaded file
     * 
     * @throws IOException
     *             if the file cannot be read
     * @throws GedcomParserException
     *             if the file cannot be parsed
     */
    @Test
    public void testWithLoadedFile() throws IOException, GedcomParserException {
        Gedcom loadedGedcom = getLoadedGedcom();

        for (Submitter original : loadedGedcom.getSubmitters().values()) {
            Submitter copy = new Submitter(original);
            assertNotSame(original, copy);
            assertEquals(original, copy);
        }
    }

    /**
     * Test with values
     */
    @Test
    public void testWithValues() {
        Submitter orig = new Submitter();
        orig.setAddress(getTestAddress());
        orig.getFaxNumbers(true).add(new StringWithCustomFacts("555-1212"));
        orig.getPhoneNumbers(true).add(new StringWithCustomFacts("555-1313"));
        orig.getWwwUrls(true).add(new StringWithCustomFacts("www.nowhere.com"));
        orig.getEmails(true).add(new StringWithCustomFacts("nobody@nowwhere.com"));
        ChangeDate cd = new ChangeDate();
        cd.setDate("22 FEB 1922");
        orig.setChangeDate(cd);
        orig.setName("Steve /Submitter/");
        orig.setRecIdNumber("123");
        orig.setRegFileNumber("345");
        orig.setXref("@SBM029@");
        orig.getLanguagePref(true).add(new StringWithCustomFacts("English"));
        orig.getLanguagePref(true).add(new StringWithCustomFacts("German"));
        orig.getCustomFacts(true).add(getTestCustomFact());
        Multimedia m = new Multimedia();
        m.setXref("@M123@");
        m.setRecIdNumber("987");
        orig.getMultimedia(true).add(new MultimediaReference(m));
        UserReference u = new UserReference();
        u.setReferenceNum("555");
        orig.getUserReferences(true).add(u);

        Submitter copy = new Submitter(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
        assertEquals(orig.toString(), copy.toString());
    }

}
