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

import org.gedcom4j.model.StringTree;
import org.gedcom4j.model.Submission;

/**
 * Parser for {@link Submission} objects
 * 
 * @author frizbog
 */
class SubmissionParser extends AbstractParser<Submission> {

    /**
     * Constructor
     * 
     * @param gedcomParser
     *            a reference to the root {@link GedcomParser}
     * @param stringTree
     *            {@link StringTree} to be parsed
     * @param loadInto
     *            the object we are loading data into
     */
    SubmissionParser(GedcomParser gedcomParser, StringTree stringTree, Submission loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    @Override
    void parse() {
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.SUBMITTER.equalsText(ch.getTag())) {
                    loadInto.setSubmitter(getSubmitter(ch.getValue()));
                } else if (Tag.FAMILY_FILE.equalsText(ch.getTag())) {
                    loadInto.setNameOfFamilyFile(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getNameOfFamilyFile());
                } else if (Tag.TEMPLE.equalsText(ch.getTag())) {
                    loadInto.setTempleCode(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getTempleCode());
                } else if (Tag.ANCESTORS.equalsText(ch.getTag())) {
                    loadInto.setAncestorsCount(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getAncestorsCount());
                } else if (Tag.DESCENDANTS.equalsText(ch.getTag())) {
                    loadInto.setDescendantsCount(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getDescendantsCount());
                } else if (Tag.ORDINANCE_PROCESS_FLAG.equalsText(ch.getTag())) {
                    loadInto.setOrdinanceProcessFlag(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getOrdinanceProcessFlag());
                } else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    loadInto.setRecIdNumber(parseStringWithCustomFacts(ch));
                    remainingChildrenAreCustomTags(ch, loadInto.getRecIdNumber());
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }
    }

}
