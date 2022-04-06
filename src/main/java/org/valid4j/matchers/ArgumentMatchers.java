package org.valid4j.matchers;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import static org.hamcrest.CoreMatchers.describedAs;
import static org.valid4j.Assertive.neverGetHereError;

/**
 * Common convenience matchers for arguments
 */
public class ArgumentMatchers {

  private ArgumentMatchers() {
    throw neverGetHereError("Prevent instantiation");
  }

  /**
   * Wraps an existing matcher, decorating its description with the name specified. All other functions are
   * delegated to the decorated matcher, including its mismatch description.
   *
   * Example:
   * <pre>named("theNameOfMyArgument", notNullValue())</pre>
   *
   * @param <T>
   *     the type of object to match
   * @param name
   *     the given name of the wrapped matcher
   * @param matcher
   *     the matcher to decorate with a name
   * @return
   *     the decorated matcher
   */
  @Factory
  public static <T> Matcher<T> named(String name, Matcher<T> matcher) {
    return describedAs("%0 = %1", matcher, name, matcher);

  }

  /**
   * A matcher matching non-null and non-empty strings
   *
   * Example:
   * <pre>assertThat("this is not an empty string", notEmptyString())</pre>
   *
   * @return
   *     matcher for a not empty string
   */
  @Factory
  public static Matcher<String> notEmptyString() {
    return new CustomTypeSafeMatcher<String>("not empty") {
      @Override
      protected boolean matchesSafely(String s) {
        return !s.isEmpty();
      }
    };
  }
}
