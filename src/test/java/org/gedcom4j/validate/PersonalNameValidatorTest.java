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
package org.gedcom4j.validate;

import org.gedcom4j.model.Individual;
import org.gedcom4j.model.PersonalName;
import org.gedcom4j.model.StringWithCustomFacts;
import org.gedcom4j.model.TestHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * Validator for {@link PersonalName} objects
 * 
 * @author frizbog
 */
public class PersonalNameValidatorTest extends AbstractValidatorTestCase {

    /**
     * Test fixture - the personal name being tested
     */
    private PersonalName pn;

    /**
     * The individual whose name(s) we are working with
     */
    private Individual ind;

    /**
     * Set up the test
     */
    @Before
    public void setUp() {
        gedcom = TestHelper.getMinimalGedcom();
        validator = new Validator(gedcom);
        validator.setAutoRepairResponder(Validator.AUTO_REPAIR_NONE);

        ind = new Individual();
        ind.setXref("@I00001@");
        gedcom.getIndividuals().put(ind.getXref(), ind);

        pn = new PersonalName();
        ind.getNames(true).add(pn);
        pn.setBasic("Joe /Schmo/");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when basic name is blank or null
     */
    @Test
    public void testBasic() {
        pn.setBasic(null);
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "basic");

        pn.setBasic("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "basic");

        pn.setBasic("       "); // whitespace gets trimmed
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "basic");

        pn.setBasic("Joe /Schmo/");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when given name is null
     */
    @Test
    public void testGivenName() {
        pn.setGivenName(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "givenName");

        pn.setGivenName("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "givenName");

        pn.setGivenName("Fred");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when nickname is null
     */
    @Test
    public void testNickname() {
        pn.setNickname(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "nickname");

        pn.setNickname("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "nickname");

        pn.setNickname("Bubba");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when surname prefix is null
     */
    @Test
    public void testNotes() {
        validator.validate();
        assertNoIssues();
        pn.getNoteStructures(true);
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when name is null
     */
    @Test
    public void testNullNameObject() {

        ind.getNames(true).add(null);
        validator.validate();
        assertFindingsContain(Severity.ERROR, ind, ProblemCode.LIST_WITH_NULL_VALUE, "names");

    }

    /**
     * Test for when prefix is null
     */
    @Test
    public void testPrefix() {
        pn.setPrefix(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "prefix");

        pn.setPrefix("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "prefix");

        pn.setPrefix("Mr.");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when suffix is null
     */
    @Test
    public void testSuffix() {
        pn.setSuffix(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "suffix");

        pn.setSuffix("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "suffix");

        pn.setSuffix("Jr.");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when surname is null
     */
    @Test
    public void testSurname() {
        pn.setSurname(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "surname");

        pn.setSurname("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "surname");

        pn.setSurname("Johnson");
        validator.validate();
        assertNoIssues();
    }

    /**
     * Test for when surname prefix is null
     */
    @Test
    public void testSurnamePrefix() {
        pn.setSurnamePrefix(new StringWithCustomFacts((String) null));
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "surnamePrefix");

        pn.setSurnamePrefix("");
        validator.validate();
        assertFindingsContain(Severity.ERROR, pn, ProblemCode.MISSING_REQUIRED_VALUE, "surnamePrefix");

        pn.setSurnamePrefix("van");
        validator.validate();
        assertNoIssues();
    }

}
