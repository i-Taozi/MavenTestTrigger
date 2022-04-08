/*
 * Copyright 2019 SPF4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.avro.calcite;

import java.util.List;
import javax.annotation.Nullable;
import org.apache.calcite.DataContext;
import org.apache.calcite.interpreter.JaninoRexCompiler;
import org.apache.calcite.interpreter.Scalar;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Zoltan Farkas
 */
public final class InterpreterUtils {

  private static final Logger LOG = LoggerFactory.getLogger(InterpreterUtils.class);

  private InterpreterUtils() { }

  @Nullable
  public static Scalar toScalar(final List<RexNode> filters,
          final RelDataType rowType, final DataContext dataContext) {
    if (filters.isEmpty()) {
      return null;
    } else {
      RexBuilder rb = new RexBuilder(dataContext.getTypeFactory());
      JaninoRexCompiler compiler = new JaninoRexCompiler(rb);
      try {
        return compiler.compile(filters, rowType).apply(dataContext);
      } catch (UnsupportedOperationException ex) {
        LOG.warn("Unable to compile filter: {}", filters, ex);
        return null;
      }
    }
  }
}
