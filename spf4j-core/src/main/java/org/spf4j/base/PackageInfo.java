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
package org.spf4j.base;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.security.CodeSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Zoltan Farkas
 */
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY") // calling Throwables.writeTo
public final class PackageInfo  {

  public static final org.spf4j.base.avro.PackageInfo NONE = new org.spf4j.base.avro.PackageInfo("", "");

  private static final LoadingCache<String, org.spf4j.base.avro.PackageInfo> CACHE = CacheBuilder.newBuilder()
          .weakKeys().weakValues().build(new CacheLoader<String, org.spf4j.base.avro.PackageInfo>() {

            @Override
            public org.spf4j.base.avro.PackageInfo load(final String key) {
              return getPackageInfoDirect(key);
            }
          });


  private PackageInfo() {
  }


  @SuppressWarnings("checkstyle:regexp")
  public static void errorNoPackageDetail(final String message, final Throwable t) {
    if (Boolean.getBoolean("spf4j.reportPackageDetailIssues")) {
      System.err.println(message);
      Throwables.writeTo(t, System.err, Throwables.PackageDetail.NONE);
    }
  }

  @Nonnull
  public static org.spf4j.base.avro.PackageInfo getPackageInfoDirect(@Nonnull final String className) {
    Class<?> aClass;
    try {
      aClass = Class.forName(className);
    } catch (Throwable ex) { // NoClassDefFoundError if class fails during init.
      errorNoPackageDetail("Error getting package detail for " + className, ex);
      return NONE;
    }
    return getPackageInfoDirect(aClass);
  }

  @Nonnull
  public static org.spf4j.base.avro.PackageInfo getPackageInfoDirect(@Nonnull final Class<?> aClass) {
    URL jarSourceUrl = getJarSourceUrl(aClass);
    final Package aPackage = aClass.getPackage();
    if (aPackage == null) {
      return NONE;
    }
    String version = aPackage.getImplementationVersion();
    return new org.spf4j.base.avro.PackageInfo(jarSourceUrl == null ? "" : jarSourceUrl.toString(),
            version  == null ? "" : version);
  }

  @Nonnull
  public static org.spf4j.base.avro.PackageInfo getPackageInfo(@Nonnull final String className) {
    return CACHE.getUnchecked(className);
  }
  /**
   * Useful to get the jar URL where a particular class is located.
   *
   * @param clasz
   * @return
   */
  @Nullable
  public static URL getJarSourceUrl(final Class<?> clasz) {
    final CodeSource codeSource = clasz.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      return null;
    } else {
      return codeSource.getLocation();
    }
  }



}
