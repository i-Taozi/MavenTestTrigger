/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.knox.gateway.webappsec.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XSSProtectionFilter implements Filter {

  public static final String X_XSS_PROTECTION    = "X-XSS-Protection";
  public static final String CUSTOM_HEADER_PARAM = "xss.protection";

  public static final String DEFAULT_VALUE = "1;mode=block";

  private String option = DEFAULT_VALUE;

  @Override
  public void init(FilterConfig config) throws ServletException {
    String customOption = config.getInitParameter(CUSTOM_HEADER_PARAM);
    if (customOption != null) {
      option = customOption;
    }
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    ((HttpServletResponse) res).setHeader(X_XSS_PROTECTION, option);
    chain.doFilter(req, new XSSProtectionResponseWrapper((HttpServletResponse) res));
  }

  @Override
  public void destroy() {
  }


  class XSSProtectionResponseWrapper extends HttpServletResponseWrapper {

    XSSProtectionResponseWrapper(HttpServletResponse res) {
      super(res);
    }

    @Override
    public void addHeader(String name, String value) {
      // don't allow additional values to be added to
      // the configured options value in topology
      if (!name.equals(X_XSS_PROTECTION)) {
        super.addHeader(name, value);
      }
    }

    @Override
    public void setHeader(String name, String value) {
      // don't allow overwriting of configured value
      if (!name.equals(X_XSS_PROTECTION)) {
        super.setHeader(name, value);
      }
    }

    @Override
    public String getHeader(String name) {
      String headerValue;
      if (name.equals(X_XSS_PROTECTION)) {
        headerValue = option;
      }
      else {
        headerValue = super.getHeader(name);
      }
      return headerValue;
    }

    @Override
    public Collection<String> getHeaderNames() {
      List<String> names = (List<String>) super.getHeaderNames();
      if (names == null) {
        names = new ArrayList<>();
      }
      names.add(X_XSS_PROTECTION);
      return names;
    }

    @Override
    public Collection<String> getHeaders(String name) {
      List<String> values = (List<String>) super.getHeaders(name);
      if (name.equals(X_XSS_PROTECTION)) {
        if (values == null) {
          values = new ArrayList<>();
        }
        values.add(option);
      }
      return values;
    }
  }
}
