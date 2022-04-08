/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
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
package org.spf4j.io;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import org.spf4j.base.CoreTextMediaType;
import org.spf4j.reflect.CachingTypeMapWrapper;
import org.spf4j.reflect.GraphTypeMap;

/**
 *
 * @author zoly
 */
@Beta
@ThreadSafe
@ParametersAreNonnullByDefault
public final class ConfigurableAppenderSupplier implements ObjectAppenderSupplier {


  private final CachingTypeMapWrapper<ObjectAppender>[] appenderMap;

  public ConfigurableAppenderSupplier() {
    this(true, (t) -> false);
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
  public ConfigurableAppenderSupplier(final boolean registerFromServiceLoader, final Predicate<Class<?>> except,
          final ObjectAppender<?>... appenders) {
    appenderMap = new CachingTypeMapWrapper[CoreTextMediaType.values().length];
    for (int i = 0; i < appenderMap.length; i++) {
      appenderMap[i] = new CachingTypeMapWrapper<>(new GraphTypeMap());
    }
    if (registerFromServiceLoader) {
      @SuppressWarnings("unchecked")
      ServiceLoader<ObjectAppender<?>> load = (ServiceLoader) ServiceLoader.load(ObjectAppender.class,
              ObjectAppender.class.getClassLoader());
      Iterator<ObjectAppender<?>> iterator = load.iterator();
      while (iterator.hasNext()) {
        ObjectAppender<?> appender = iterator.next();
        Class<?> appenderType = getAppenderType(appender);
        if (!except.test(appenderType)) {
          if (!ConfigurableAppenderSupplier.this.tryRegister((Class) appenderType, (ObjectAppender) appender)) {
            throw new IllegalArgumentException("Attempting to register duplicate appender(" + appender + ") for "
                    + appenderType);
          }
        }
      }
    }
    for (ObjectAppender<?> appender : appenders) {
      if (!ConfigurableAppenderSupplier.this.tryRegister(getAppenderType(appender), (ObjectAppender) appender)) {
        throw new IllegalArgumentException("Cannot register appender " + appender);
      }
    }
    appenderMap[ObjectAppender.TOSTRING_APPENDER.getAppendedType().ordinal()]
            .putIfNotPresent(Object.class, ObjectAppender.TOSTRING_APPENDER);
  }

  public static Class<?> getAppenderType(final ObjectAppender<?> appender) {

    Class<?> aClass = appender.getClass();
    Class<?> appenderType = null;
    do {
      Type[] genericInterfaces = aClass.getGenericInterfaces();
      for (Type type : genericInterfaces) {
        if (type instanceof ParameterizedType) {
          ParameterizedType pType = (ParameterizedType) type;
          if (pType.getRawType() == ObjectAppender.class) {
            Type actualTypeArgument = pType.getActualTypeArguments()[0];
            if (actualTypeArgument instanceof ParameterizedType) {
              appenderType = (Class) ((ParameterizedType) actualTypeArgument).getRawType();
            } else {
              appenderType = (Class) actualTypeArgument;
            }
            break;
          }
        }
      }
      aClass = aClass.getSuperclass();
    } while (appenderType == null && aClass != null);
    if (appenderType == null) {
      throw new IllegalArgumentException("Improperly declared Appender " + appender);
    }
    return appenderType;
  }

  @SuppressWarnings("unchecked")
  public <T> int register(final Class<T> type, final ObjectAppender<? super T>... appenders) {
    int i = 0;
    for (ObjectAppender<? super T> appender : appenders) {
      if (ConfigurableAppenderSupplier.this.tryRegister(type, appender)) {
        i++;
      }
    }
    return i;
  }

  public <T> void replace(final CoreTextMediaType mt, final Class<T> type,
          final Function<ObjectAppender<? super T>, ObjectAppender<? super T>> replace) {
    appenderMap[mt.ordinal()].replace(type, (Function) replace);
  }

  public <T> void register(final Class<T> type, final ObjectAppender<? super T> appender) {
    if (!tryRegister(type, appender)) {
      throw new IllegalArgumentException("Cannnot Register " + appender + " for " + type);
    }
  }

  @CheckReturnValue
  public <T> boolean tryRegister(final Class<T> type, final ObjectAppender<? super T> appender) {
    return appenderMap[appender.getAppendedType().ordinal()].putIfNotPresent(type, appender);
  }

  public <T> void register(final Class<T> type, final CoreTextMediaType contentType,
          final ObjectAppender<? super T> appender) {
     if (!tryRegister(type, contentType, appender)) {
       throw new IllegalArgumentException("Cannnot Register " + appender + " for " + type);
     }
  }

  @CheckReturnValue
  public <T> boolean tryRegister(final Class<T> type, final CoreTextMediaType contentType,
          final ObjectAppender<? super T> appender) {
    return appenderMap[contentType.ordinal()].putIfNotPresent(type,
            new ObjectAppenderContentTypeAdapter(appender, contentType));
  }


  public boolean unregister(final Class<?> type) {
    boolean result = false;
    for (int i = 0, l = CoreTextMediaType.values().length;  i < l; i++) {
      boolean removed = appenderMap[i].remove(type);
      if (removed) {
        result = true;
      }
    }
    return result;
  }

  @Override
  public ObjectAppender get(final CoreTextMediaType mt,  final Type type) {
    return appenderMap[mt.ordinal()].get(type);
  }

  @Override
  public String toString() {
    return "ConfigurableAppenderSupplier{" + "lookup=" + Arrays.toString(appenderMap)  + '}';
  }

  private static final class ObjectAppenderContentTypeAdapter<T> implements ObjectAppender<T> {

    private final ObjectAppender<? super T> appender;
    private final CoreTextMediaType contentType;

    ObjectAppenderContentTypeAdapter(final ObjectAppender<? super T> appender, final CoreTextMediaType contentType) {
      this.appender = appender;
      this.contentType = contentType;
    }

    @Override
    public void append(final T object, final Appendable appendTo) throws IOException {
      appender.append(object, appendTo);
    }

    @Override
    public void append(final T object, final Appendable appendTo, final ObjectAppenderSupplier appenderSupplier)
            throws IOException {
      appender.append(object, appendTo, appenderSupplier);
    }

    @Override
    public CoreTextMediaType getAppendedType() {
      return contentType;
    }
  }

}