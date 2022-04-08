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

import java.io.Serializable;

/**

 * @author Zoltan Farkas
 */
public class RemoteException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String remoteClass;

  private final String origin;

  private final Serializable remoteDetail;

  public RemoteException(final String message, final String origin,
          final String remoteClass, final Serializable remoteDetail) {
    this(message, origin, remoteClass, remoteDetail, null);
  }

  protected RemoteException(final String message, final String origin,
          final String remoteClass,
          final Serializable remoteDetail, final Throwable cause) {
    super(message, cause);
    this.origin = origin;
    this.remoteDetail = remoteDetail;
    this.remoteClass = remoteClass;
  }

  public final String getOrigin() {
    return origin;
  }

  public final Serializable getRemoteDetail() {
    return remoteDetail;
  }

  public final String getRemoteClass() {
    return remoteClass;
  }

  /**
   * @return string representation. (Class name + origin + message)
   */
  @Override
  public String toString() {
    return "RemoteException:" + getRemoteClass() +  '@' + origin + ':' + this.getMessage();
  }

}
