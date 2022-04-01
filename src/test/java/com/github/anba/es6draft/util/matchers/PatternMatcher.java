/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.util.matchers;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link Matcher} to match strings against regular expressions.
 */
public class PatternMatcher extends TypeSafeMatcher<String> {
    private final String regex;

    public PatternMatcher(String regex) {
        this.regex = regex;
    }

    public static Matcher<String> matchesPattern(String regex) {
        return new PatternMatcher(regex);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("string matching '%s'", regex));
    }

    @Override
    protected void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(item);
    }

    @Override
    protected boolean matchesSafely(String item) {
        return getPattern().matcher(item).find();
    }

    private Pattern getPattern() {
        return Pattern.compile(regex);
    }
}
