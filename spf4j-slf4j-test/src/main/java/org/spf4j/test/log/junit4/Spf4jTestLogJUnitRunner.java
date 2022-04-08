/*
 * Copyright 2018 SPF4J.
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
package org.spf4j.test.log.junit4;

import java.lang.reflect.Field;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author Zoltan Farkas
 */
@ThreadSafe
public class Spf4jTestLogJUnitRunner extends BlockJUnit4ClassRunner {

  private static final Field LISTERES_FIELD = getListenersField();

  public Spf4jTestLogJUnitRunner(final Class<?> klass) throws InitializationError {
    super(klass);
  }

  /**
   * See JUnit doc if overwriting...
   * @param notifier
   */
  @Override
  public synchronized void run(final RunNotifier notifier) {
    if (LISTERES_FIELD != null) {
      try {
        List<RunListener> lstnrs =  (List<RunListener>) LISTERES_FIELD.get(notifier);
        for (RunListener lstnr : lstnrs) {
          if (lstnr instanceof Spf4jTestLogRunListener) {
            super.run(notifier);
            return;
          }
        }
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }
    Spf4jTestLogRunListenerSingleton listener = Spf4jTestLogRunListenerSingleton.getOrCreateListenerInstance();
    notifier.removeListener(listener);
    notifier.addListener(listener);
    super.run(notifier);
  }

  @Nullable
  private static Field getListenersField() {
    Field field;
    try {
      field = RunNotifier.class.getDeclaredField("listeners");
    } catch (NoSuchFieldException | SecurityException ex) {
      return null;
    }
    field.setAccessible(true);
    return field;
  }

}
