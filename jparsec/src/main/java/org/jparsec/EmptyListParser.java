/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.jparsec;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser that always returns an empty mutable list.
 * 
 * @author Ben Yu
 */
final class EmptyListParser<T> extends Parser<List<T>> {
  @SuppressWarnings("rawtypes")
  private static final EmptyListParser INSTANCE = new EmptyListParser();
  
  @SuppressWarnings("unchecked")
  static <T> Parser<List<T>> instance() {
    return INSTANCE;
  }
  
  private EmptyListParser() {}
  
  @Override boolean apply(ParseContext ctxt) {
    ctxt.result = new ArrayList<T>(0);
    return true;
  }
  
  @Override public String toString() {
    return "[]";
  }
}
