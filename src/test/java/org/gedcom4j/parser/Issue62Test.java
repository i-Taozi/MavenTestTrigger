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
package org.gedcom4j.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.gedcom4j.exception.GedcomParserException;
import org.gedcom4j.model.Gedcom;
import org.gedcom4j.model.Individual;
import org.gedcom4j.model.IndividualEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for Issue 62, where events have descriptions and continuation lines like FTM exports, even though the spec doesn't
 * allow them.
 * 
 * @author frizbog
 */
public class Issue62Test {

    /**
     * The birth event from the GEDCOM file. In the test file, this tag has lengthy text with concatenation lines in violation of
     * the spec.
     */
    private IndividualEvent birth;

    /**
     * The cremation event from the GEDCOM file. In the test file, this tag has a Y in the [Y|&lt;NULL&gt;] field.
     */
    private IndividualEvent cremation;

    /**
     * The burial event from the test GEDCOM file. In the file, this tag has nothing (i.e., "null") in the [Y|&lt;NULL&gt;] field.
     */
    private IndividualEvent burial;

    /**
     * The death event from the test GEDCOM file. In the file, this tag has a single line of descriptive text, in violation of the
     * spec.
     */
    private IndividualEvent death;

    /**
     * Parse the GEDCOM and set up fields for use in test cases
     * 
     * @throws IOException
     *             if the file can't be read
     * @throws GedcomParserException
     *             if there's a parsing problem
     */
    @Before
    public void setUp() throws IOException, GedcomParserException {
        GedcomParser gp = new GedcomParser();
        gp.load("sample/Event Tag Test.ged");
        Gedcom g = gp.getGedcom();
        assertNotNull(g);
        assertTrue(gp.getErrors().isEmpty());
        assertEquals("Two tags had descriptions where [Y|<NULL>] belonged", 2, gp.getWarnings().size());
        assertFalse(g.getIndividuals().isEmpty());
        assertEquals(1, g.getIndividuals().size());
        Individual i = g.getIndividuals().values().iterator().next();
        assertNotNull(i);
        assertEquals(4, i.getEvents().size());
        birth = i.getEvents().get(0);
        assertNotNull(birth);
        cremation = i.getEvents().get(1);
        assertNotNull(cremation);
        burial = i.getEvents().get(2);
        assertNotNull(burial);
        death = i.getEvents().get(3);
        assertNotNull(death);
    }

    /**
     * The multi-line description following the BIRT tag should be in the description field
     */
    @Test
    public void testBirthDescription() {
        assertNotNull(birth.getDescription());
        assertNotNull(birth.getDescription().getValue());
        assertTrue(birth.getDescription().getValue().startsWith("This was entered"));
        assertTrue(birth.getDescription().getValue().endsWith("laborum."));
        assertEquals(552, birth.getDescription().getValue().length());
    }

    /**
     * yNull should always be Y or "null" (not necessarily Java's idea of null, but just empty/missing in the GEDCOM). In the test
     * file, there was a lengthy description following the BIRT tag (with a bunch of CONC lines), so Y was not specified, so this
     * should have been treated as a "null" in the GEDCOM.
     */
    @Test
    public void testBirthYNull() {
        assertNull("", birth.getYNull());
    }

    /**
     * The BURI tag had a single line of description following it
     */
    @Test
    public void testBurialDescription() {
        assertNotNull(burial.getDescription());
        assertEquals("Unmarked grave", burial.getDescription().getValue());
    }

    /**
     * yNull should always be Y or "null" (not necessarily Java's idea of null, but just empty/missing in the GEDCOM). In the test
     * file, the BURI tag had nothing after it
     */
    @Test
    public void testBurialYNull() {
        assertNull(burial.getYNull());
    }

    /**
     * The CREM tag has no description value, since it had a Y value in the [Y|&lt;NULL&gt;] field.
     */
    @Test
    public void testCremationDescription() {
        assertNull(cremation.getDescription());
    }

    /**
     * yNull should always be Y or "null" (not necessarily Java's idea of null, but just empty/missing in the GEDCOM). In the test
     * file, the CREM tag had a Y.
     */
    @Test
    public void testCremationYNull() {
        assertEquals("Y", cremation.getYNull());
    }

    /**
     * The DEAT tag has no description value since it had nothing after the tag
     */
    @Test
    public void testDeathDescription() {
        assertNull(death.getDescription());
    }

    /**
     * yNull should always be Y or "null" (not necessarily Java's idea of null, but just empty/missing in the GEDCOM). In the test
     * file, there was a description following the DEAT tag, so Y was not specified, so this should have been treated as a "null" in
     * the GEDCOM.
     */
    @Test
    public void testDeathYNull() {
        assertNull(death.getYNull());
    }
}
