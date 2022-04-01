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

import org.gedcom4j.model.FamilyChild;
import org.gedcom4j.model.FamilySpouse;

/**
 * Validator for {@link FamilyChild}
 * 
 * @author frizbog
 */
public class FamilySpouseValidator extends AbstractValidator {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -4229277765495507049L;

    /**
     * The {@link FamilyChild} object being validated
     */
    private final FamilySpouse fs;

    /**
     * Instantiates a new family child validator.
     *
     * @param validator
     *            the base validator that tracks all the findings
     * @param fs
     *            the family-spouse linkage structure
     */
    public FamilySpouseValidator(Validator validator, FamilySpouse fs) {
        super(validator);
        this.fs = fs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate() {
        checkNotes(fs);
        mustHaveValue(fs, "family");
    }

}
