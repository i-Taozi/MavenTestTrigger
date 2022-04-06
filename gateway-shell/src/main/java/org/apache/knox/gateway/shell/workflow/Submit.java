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
package org.apache.knox.gateway.shell.workflow;

import com.jayway.jsonpath.JsonPath;
import org.apache.knox.gateway.shell.AbstractRequest;
import org.apache.knox.gateway.shell.BasicResponse;
import org.apache.knox.gateway.shell.KnoxSession;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

class Submit {

  public static class Request extends AbstractRequest<Response> {

    private String text;
    private String file;
    private String action = "start";

    Request( KnoxSession session ) {
      super( session );
    }

    public Request text( String text ) {
      this.text = text;
      return this;
    }

    public Request file( String file ) {
      this.file = file;
      return this;
    }

    public Request action( String action ) {
      this.action = action;
      return this;
    }

    @Override
    protected Callable<Response> callable() {
      return new Callable<Response>() {
        @Override
        public Response call() throws Exception {
          URIBuilder uri = uri( Workflow.SERVICE_PATH, "/jobs" );
          addQueryParam( uri, "action", action );
          HttpPost request = new HttpPost( uri.build() );
          HttpEntity entity = null;
          if( text != null ) {
            entity = new StringEntity( text, ContentType.create( "application/xml", StandardCharsets.UTF_8 ) );
          } else if( file != null ) {
            entity = new FileEntity( new File( file ), ContentType.create( "application/xml" ) );
          }
          request.setEntity( entity );
          return new Response( execute( request ) );
        }
      };
    }

  }

  public static class Response extends BasicResponse {

    Response( HttpResponse response ) {
      super( response );
    }

    public String getJobId() throws IOException {
      return JsonPath.read( getString(), "$.id" );
    }

  }

}
