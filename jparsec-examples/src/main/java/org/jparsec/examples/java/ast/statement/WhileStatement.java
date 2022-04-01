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
package org.jparsec.examples.java.ast.statement;

import org.jparsec.examples.common.ValueObject;
import org.jparsec.examples.java.ast.expression.Expression;

/**
 * Represents the "while () ..." statement.
 * 
 * @author Ben Yu
 */
public final class WhileStatement extends ValueObject implements Statement {
  public final Expression condition;
  public final Statement statement;
  
  public WhileStatement(Expression condition, Statement statement) {
    this.condition = condition;
    this.statement = statement;
  }
  
  @Override public String toString() {
    return "while (" + condition + ") " + statement;
  }
}
