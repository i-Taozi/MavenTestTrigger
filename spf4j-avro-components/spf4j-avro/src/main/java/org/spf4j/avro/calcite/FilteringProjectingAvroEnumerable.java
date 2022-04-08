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

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.avro.generic.IndexedRecord;
import org.apache.calcite.DataContext;
import org.apache.calcite.interpreter.Scalar;
import org.apache.calcite.interpreter.Spf4jDataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.spf4j.base.CloseableIterator;

/**
 * @author Zoltan Farkas
 */
@ParametersAreNonnullByDefault
class FilteringProjectingAvroEnumerable extends AbstractEnumerable<Object[]> {

  private final Object[] rawRow;
  private final Spf4jDataContext spf4jDataContext;
  private final Scalar filterExpression;
  private final int[] projection;
  private final Supplier<CloseableIterator<? extends IndexedRecord>> stream;
  private final Supplier<Boolean> cancelFlag;

  FilteringProjectingAvroEnumerable(final org.apache.avro.Schema componentType,
          final DataContext root,
          @Nullable final Scalar filters, @Nullable final int[] projection,
          final Supplier<CloseableIterator<? extends IndexedRecord>> stream) {
    this.rawRow = new Object[componentType.getFields().size()];
    this.spf4jDataContext = new Spf4jDataContext(root);
    filterExpression = filters;
    this.projection = projection;
    this.stream = stream;
    AtomicBoolean contextFlag = DataContext.Variable.CANCEL_FLAG.get(root);
    cancelFlag = contextFlag == null ? () -> Boolean.FALSE : contextFlag::get;
  }

  public Enumerator<Object[]> enumerator() {
    return new Enumerator<Object[]>() {
      private Object[] current = null;

      private CloseableIterator<? extends IndexedRecord> iterator = stream.get();

      @Override
      public Object[] current() {
        if (current == null) {
          throw new IllegalStateException("Use moveNext on " + this);
        }
        return current;
      }

      @Override
      public boolean moveNext() {
        if (cancelFlag.get()) {
          throw new CancellationException("Operation cancelled on " + stream + " at " + Arrays.toString(current));
        }
        if (iterator.hasNext()) {
          while (true) {
            IndexedRecord ir = iterator.next();
            IndexedRecords.copyRecord(ir, rawRow);
            spf4jDataContext.values = rawRow;
            boolean match;
            if (filterExpression == null) {
              match = true;
            } else {
              Boolean result = (Boolean) filterExpression.execute(spf4jDataContext);
              if (result == null) {
                throw new IllegalStateException("Filter expression cannot evaluate to null: " + filterExpression);
              }
              match = result;
            }
            if (match) {
              break;
            }
            if (!iterator.hasNext()) {
              current = null;
              return false;
            }
          }
          if (projection == null) {
            current = rawRow.clone();
          } else {
            current = new Object[projection.length];
            for (int i = 0; i < projection.length; i++) {
              current[i] = rawRow[projection[i]];
            }
          }
          return true;
        } else {
          current = null;
          return false;
        }
      }

      @Override
      public void reset() {
        iterator.close();
        iterator = stream.get();
        current = null;
      }

      @Override
      public void close() {
        iterator.close();
      }
    };
  }

  @Override
  public String toString() {
    return "FilteringProjectingAvroEnumerable{" + "rawRow=" + Arrays.toString(rawRow)
            + ", spf4jDataContext=" + spf4jDataContext + ", filterExpression="
            + filterExpression + ", projection=" + Arrays.toString(projection) + ", stream="
            + stream + ", cancelFlag=" + cancelFlag + '}';
  }



}
