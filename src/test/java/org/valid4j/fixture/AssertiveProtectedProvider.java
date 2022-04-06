package org.valid4j.fixture;

import org.valid4j.provider.AssertiveProvider;
import org.valid4j.provider.CheckPolicy;
import org.valid4j.provider.UnreachablePolicy;

/**
 * Protected assertive policy provider. Used to demonstrate that initialization of a protected
 * assertive policy provider, will result in initialization error.
 */
class AssertiveProtectedProvider implements AssertiveProvider {

  @Override
  public CheckPolicy requirePolicy() {
    return null;
  }

  @Override
  public CheckPolicy ensurePolicy() {
    return null;
  }

  @Override
  public UnreachablePolicy neverGetHerePolicy() {
    return null;
  }
}
