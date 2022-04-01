/*
 * Copyright (c) 2016 Mark A. Sikes
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

import java.util.ArrayList;
import java.util.List;

import org.gedcom4j.Options;

/**
 * @author Mark A Sikes
 *
 */
public abstract class AbstractNotesElement extends AbstractElement implements HasNotes {

    /** Serial version uid */
    private static final long serialVersionUID = 2539148787102235445L;

    /**
     * NoteStructure structures on this element
     */
    private List<NoteStructure> noteStructures = getNoteStructures(Options.isCollectionInitializationEnabled());

    /** Default constructor */
    public AbstractNotesElement() {
        // Default constructor does nothing
    }

    /**
     * Copy constructor
     * 
     * @param other
     *            the other object being copeied
     */
    public AbstractNotesElement(AbstractNotesElement other) {
        super(other);
        if (other.getNoteStructures() != null) {
            noteStructures = new ArrayList<>();
            for (NoteStructure n : other.getNoteStructures()) {
                noteStructures.add(new NoteStructure(n));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractNotesElement other = (AbstractNotesElement) obj;
        if (noteStructures == null) {
            if (other.noteStructures != null) {
                return false;
            }
        } else if (!noteStructures.equals(other.noteStructures)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the noteStructures.
     *
     * @return the noteStructures
     */
    @Override
    public List<NoteStructure> getNoteStructures() {
        return noteStructures;
    }

    /**
     * Get the noteStructures
     * 
     * @param initializeIfNeeded
     *            true if this collection should be created on-the-fly if it is currently null
     * @return the noteStructures
     */
    @Override
    public List<NoteStructure> getNoteStructures(boolean initializeIfNeeded) {
        if (initializeIfNeeded && noteStructures == null) {
            noteStructures = new ArrayList<>(0);
        }
        return noteStructures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (noteStructures == null ? 0 : noteStructures.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append(" [");
        if (noteStructures != null) {
            builder.append("noteStructures=");
            builder.append(noteStructures);
            builder.append(", ");
        }
        if (getCustomFacts() != null) {
            builder.append("customFacts=");
            builder.append(getCustomFacts());
        }
        builder.append("]");
        return builder.toString();
    }
}
