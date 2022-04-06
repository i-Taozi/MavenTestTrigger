/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.filter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XForwardedHeaderRequestWrapper extends GatewayRequestWrapper {

  private static final String X_FORWARDED_FOR = "X-Forwarded-For";
  private static final String X_FORWARDED_FOR_LOWER = X_FORWARDED_FOR.toLowerCase(Locale.ROOT);
  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  private static final String X_FORWARDED_PROTO_LOWER = X_FORWARDED_PROTO.toLowerCase(Locale.ROOT);
  private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
  private static final String X_FORWARDED_PORT_LOWER = X_FORWARDED_PORT.toLowerCase(Locale.ROOT);
  private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
  private static final String X_FORWARDED_HOST_LOWER = X_FORWARDED_HOST.toLowerCase(Locale.ROOT);
  private static final String X_FORWARDED_SERVER = "X-Forwarded-Server";
  private static final String X_FORWARDED_SERVER_LOWER = X_FORWARDED_SERVER.toLowerCase(Locale.ROOT);
  private static final String X_FORWARDED_CONTEXT = "X-Forwarded-Context";
  private static final String X_FORWARDED_CONTEXT_LOWER = X_FORWARDED_CONTEXT.toLowerCase(Locale.ROOT);
  private static final List<String> headerNames = new ArrayList<>();

  static {
    headerNames.add(X_FORWARDED_FOR);
    headerNames.add(X_FORWARDED_PROTO);
    headerNames.add(X_FORWARDED_PORT);
    headerNames.add(X_FORWARDED_HOST);
    headerNames.add(X_FORWARDED_SERVER);
    headerNames.add(X_FORWARDED_CONTEXT);
  }

  Map<String,String> proxyHeaders = new HashMap<>();

  public XForwardedHeaderRequestWrapper(HttpServletRequest request) {
    super( request );
    setHeaders(request, false, null);
  }

  public XForwardedHeaderRequestWrapper(HttpServletRequest request, final boolean isAppendServiceName, final String serviceContext) {
    super( request );
    setHeaders(request, isAppendServiceName, serviceContext);
  }

  private void setHeaders(final HttpServletRequest request, final boolean isAppendServiceName, final String serviceContext) {
    setHeader( X_FORWARDED_FOR_LOWER, getForwardedFor( request ) );
    setHeader( X_FORWARDED_PROTO_LOWER, getForwardedProto( request ) );
    setHeader( X_FORWARDED_PORT_LOWER, getForwardedPort( request ) );
    setHeader( X_FORWARDED_HOST_LOWER, getForwardedHost( request ) );
    setHeader( X_FORWARDED_SERVER_LOWER, getForwardedServer( request ) );
    setHeader( X_FORWARDED_CONTEXT_LOWER, getForwardedContext( request, isAppendServiceName, serviceContext) );
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return new CompositeEnumeration<>( Collections.enumeration(headerNames), super.getHeaderNames() );
  }

  @Override
  public Enumeration<String> getHeaders( String name ) {
    name = name.toLowerCase(Locale.ROOT);
    Enumeration<String> values;
    String value = proxyHeaders.get( name );
    if( value != null ) {
      values = Collections.enumeration(Collections.singletonList(value));
    } else {
      values = super.getHeaders( name );
    }
    return values;
  }

  @Override
  public String getHeader( String name ) {
    name = name.toLowerCase(Locale.ROOT);
    String value = proxyHeaders.get( name );
    if( value == null ) {
      value = super.getHeader( name );
    }
    return value;
  }

  private void setHeader( String name, String value ) {
    if( name != null && value != null ) {
      proxyHeaders.put( name, value );
    }
  }

  private static String getForwardedFor( HttpServletRequest request ) {
    String value;
    String curr = request.getHeader( X_FORWARDED_FOR );
    String addr = request.getRemoteAddr();
    if( curr == null ) {
      value = addr;
    } else {
      value = curr + "," + addr;
    }
    return value;
  }

  private static String getForwardedProto( HttpServletRequest request ) {
    String value = request.getHeader( X_FORWARDED_PROTO );
    if( value == null ) {
      value = request.isSecure() ? "https" : "http";
    }
    return value;
  }

  private static String getForwardedPort( HttpServletRequest request ) {
    String value = request.getHeader( X_FORWARDED_PORT );
    if( value == null ) {
      String forwardedHost = getForwardedHost( request );
      int separator = forwardedHost.indexOf(':');
      if ( separator > 0 ) {
          value = forwardedHost.substring(separator + 1, forwardedHost.length());
      } else {
          // use default ports
          value = request.isSecure() ? "443" : "80";
      }
    }
    return value;
  }

  private static String getForwardedHost( HttpServletRequest request ) {
    String value = request.getHeader( X_FORWARDED_HOST );
    if( value == null ) {
      value = request.getHeader( "Host" );
    }
    return value;
  }

  private static String getForwardedServer( HttpServletRequest request ) {
    return request.getServerName();
  }

  private static String getForwardedContext( HttpServletRequest request, final boolean isAppendServiceName, final String serviceContext) {
    String remote = request.getHeader( X_FORWARDED_CONTEXT );
    String local;
    /* prefer parameter defined in topology over the gateway-site.xml property */
    if(serviceContext != null && !serviceContext.isEmpty()) {
      local = request.getContextPath() + "/" + serviceContext;
    }
    else if(isAppendServiceName) {
      local = request.getContextPath() + "/" + extractServiceName(request.getContextPath(), request.getRequestURI());
    } else {
      local = request.getContextPath();
    }
    return ( remote == null ? "" : remote ) + ( local == null ? "" : local );
  }

  /**
   * Given a URI path and context, this function extracts service name
   *
   * @param originalContext {gatewayName}/{topologyname}
   * @param path            request URI
   * @return service name
   * @since 1.3.0
   */
  private static String extractServiceName(final String originalContext,
      final String path) {
    final String[] sub = path.split(originalContext);
    String serviceName = "";

    if (sub.length > 1) {
      final String[] paths = sub[1].split("/");
      serviceName = paths[1];
    }
    return serviceName;
  }

}
