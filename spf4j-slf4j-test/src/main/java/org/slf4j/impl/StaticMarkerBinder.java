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
package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * @author Zoltan Farkas
 */
public final class StaticMarkerBinder implements MarkerFactoryBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

  private final IMarkerFactory markerFactory = new BasicMarkerFactory();

  private StaticMarkerBinder() {
  }

  /**
   * Return the singleton of this class.
   *
   * @return the StaticMarkerBinder singleton
   * @since 1.7.14
   */
  public static StaticMarkerBinder getSingleton() {
    return SINGLETON;
  }

  /**
   * Currently this method always returns an instance of {@link BasicMarkerFactory}.
   */
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  /**
   * Currently, this method returns the class name of {@link BasicMarkerFactory}.
   */
  public String getMarkerFactoryClassStr() {
    return BasicMarkerFactory.class.getName();
  }

  @Override
  public String toString() {
    return "StaticMarkerBinder{" + "markerFactory=" + markerFactory + '}';
  }

}
