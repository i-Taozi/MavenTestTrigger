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

import org.gedcom4j.model.Submission;

/**
 * A validator for a {@link Submission}
 * 
 * @author frizbog
 */
public class SubmissionValidator extends AbstractValidator {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -1499277826314233624L;

    /**
     * The Submission being validated
     */
    private final Submission submission;

    /**
     * Constructor
     * 
     * @param validator
     *            the root/main validator
     * @param submission
     *            the submission being validated
     */
    public SubmissionValidator(Validator validator, Submission submission) {
        super(validator);
        this.submission = submission;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate() {
        xrefMustBePresentAndWellFormed(submission);
        mustHaveValueOrBeOmitted(submission, "ancestorsCount");
        mustHaveValueOrBeOmitted(submission, "descendantsCount");
        mustHaveValueOrBeOmitted(submission, "nameOfFamilyFile");
        mustHaveValueOrBeOmitted(submission, "ordinanceProcessFlag");
        if (submission.getOrdinanceProcessFlag() != null && submission.getOrdinanceProcessFlag().getValue() != null && !"yes"
                .equals(submission.getOrdinanceProcessFlag().getValue()) && !"no".equals(submission.getOrdinanceProcessFlag()
                        .getValue())) {
            newFinding(submission, Severity.ERROR, ProblemCode.ILLEGAL_VALUE, "ordinanceProcessFlag");
        }
        mustHaveValueOrBeOmitted(submission, "recIdNumber");
        mustHaveValueOrBeOmitted(submission, "templeCode");
    }

}
