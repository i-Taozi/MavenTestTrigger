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

import org.gedcom4j.model.AbstractCitation;
import org.gedcom4j.model.CitationWithSource;
import org.gedcom4j.model.CitationWithoutSource;
import org.gedcom4j.model.NoteStructure;
import org.gedcom4j.model.Source;
import org.junit.Test;

/**
 * @author frizbog1
 */
public class CitationValidatorTest extends AbstractValidatorTestCase {

    /**
     * Test method for {@link org.gedcom4j.validate.CitationValidator#validate()}.
     */
    @Test
    public void testValidateWithoutSourceNoNoteLines() {
        NoteStructure n = new NoteStructure();
        CitationWithoutSource c = new CitationWithoutSource();
        c.getNoteStructures(true).add(n);
        AbstractValidator cv = new CitationValidator(validator, c);
        cv.validate();
        assertFindingsContain(Severity.ERROR, n, ProblemCode.MISSING_REQUIRED_VALUE.getCode(), "lines");
    }

    /**
     * Test method for {@link org.gedcom4j.validate.CitationValidator#validate()}.
     */
    @Test
    public void testValidateWithoutSourceSimple() {
        NoteStructure n = new NoteStructure();
        n.getLines(true).add("Frying Pan");
        CitationWithoutSource c = new CitationWithoutSource();
        c.getNoteStructures(true).add(n);
        AbstractValidator cv = new CitationValidator(validator, c);
        cv.validate();
        assertNoIssues();
    }

    /**
     * Test method for {@link org.gedcom4j.validate.CitationValidator#validate()}.
     */
    @Test
    public void testValidateWithSourceNoSource() {
        NoteStructure n = new NoteStructure();
        n.getLines(true).add("Frying Pan");
        AbstractCitation c = new CitationWithSource();
        c.getNoteStructures(true).add(n);
        AbstractValidator cv = new CitationValidator(validator, c);
        cv.validate();
        assertFindingsContain(Severity.ERROR, c, ProblemCode.MISSING_REQUIRED_VALUE.getCode(), "source");
    }

    /**
     * Test method for {@link org.gedcom4j.validate.CitationValidator#validate()}.
     */
    @Test
    public void testValidateWithSourceSimple() {
        NoteStructure n = new NoteStructure();
        n.getLines(true).add("Frying Pan");
        CitationWithSource c = new CitationWithSource();
        c.getNoteStructures(true).add(n);
        Source source = new Source();
        c.setSource(source);

        AbstractValidator cv = new CitationValidator(validator, c);
        cv.validate();
        assertNoIssues();
    }

}
