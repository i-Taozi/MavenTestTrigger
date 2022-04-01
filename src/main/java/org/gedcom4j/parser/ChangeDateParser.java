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

import java.util.List;

import org.gedcom4j.model.ChangeDate;
import org.gedcom4j.model.NoteStructure;
import org.gedcom4j.model.StringTree;
import org.gedcom4j.model.StringWithCustomFacts;

/**
 * A parser for {@link ChangeDate} objects
 * 
 * @author frizbog
 */
class ChangeDateParser extends AbstractParser<ChangeDate> {
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
    ChangeDateParser(GedcomParser gedcomParser, StringTree stringTree, ChangeDate loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void parse() {
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.DATE.equalsText(ch.getTag())) {
                    loadInto.setDate(new StringWithCustomFacts(ch.getValue()));
                    if (ch.getChildren() != null) {
                        for (StringTree gch : ch.getChildren()) {
                            if ("TIME".equals(gch.getTag())) {
                                loadInto.setTime(parseStringWithCustomFacts(gch));
                            } else {
                                unknownTag(gch, loadInto.getDate());
                            }
                        }
                    }
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<NoteStructure> notes = loadInto.getNoteStructures(true);
                    new NoteStructureListParser(gedcomParser, ch, notes).parse();
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }

    }

}
