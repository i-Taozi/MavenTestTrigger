/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.susom.database;

import java.util.function.Supplier;

/**
 * A block of runnable code using a transacted Database.
 *
 * @author garricko
 */
public interface DbCodeTypedTx<T> {
  /**
   * Implement this method to provide a block of code that uses the provided database
   * and is transacted. Whether the transaction will commit or rollback is typically
   * controlled by the code that invokes this method.
   *
   * <p>If a {@link Throwable} is thrown from this method, it will be caught, wrapped in
   * a DatabaseException (if it is not already one), and then propagated.</p>
   */
  T run(Supplier<Database> db, Transaction tx) throws Exception;
}
