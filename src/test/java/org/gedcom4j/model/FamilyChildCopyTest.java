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

import org.gedcom4j.model.enumerations.AdoptedByWhichParent;
import org.junit.Test;

/**
 * Test copy constructor for {@link FamilyChild}
 * 
 * @author frizbog
 */
public class FamilyChildCopyTest extends AbstractCopyTest {

    /**
     * Test copying a null {@link FamilyChild}, which should never work
     */
    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testCopyNull() {
        new FamilyChild(null);
    }

    /**
     * Test the simplest possible scenario - copy a new default {@link FamilyChild}
     */
    @Test
    public void testSimplestPossible() {
        FamilyChild orig = new FamilyChild();
        FamilyChild copy = new FamilyChild(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
    }

    /**
     * Test with values
     */
    @Test
    public void testWithValues() {
        FamilyChild orig = new FamilyChild();
        orig.setAdoptedBy(AdoptedByWhichParent.WIFE);
        Family f = new Family();
        Individual h = new Individual();
        h.setXref("@I1@");
        f.setHusband(new IndividualReference(h));
        Individual w = new Individual();
        w.setXref("@I2@");
        f.setWife(new IndividualReference(w));
        Individual k = new Individual();
        k.setXref("@I3@");
        f.getChildren(true).add(new IndividualReference(k));
        orig.setFamily(f);
        orig.setPedigree("OMG");
        orig.setStatus("AOK");
        orig.getCustomFacts(true).add(getTestCustomFact());
        orig.getNoteStructures(true).add(getTestNoteStructure());

        FamilyChild copy = new FamilyChild(orig);
        assertEquals(orig, copy);
        assertNotSame(orig, copy);
        assertEquals(orig.toString(), copy.toString());
    }

}
