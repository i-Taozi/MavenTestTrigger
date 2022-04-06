/***********************************************************************************************************************
 *
 * Elasticsearch IMAP/Pop3 E-Mail Importer
 * ==========================================
 *
 * Copyright (C) 2014 by Hendrik Saly (http://saly.de) and others.
 * 
 * Contains (partially) copied code from Jörg Prante's Elasticsearch JDBC river (https://github.com/jprante/elasticsearch-river-jdbc)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * $Id:$
 *
 **********************************************************************************************************************/
package de.saly.elasticsearch.importer.imap.maildestination;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;

import de.saly.elasticsearch.importer.imap.impl.IMAPImporter;
import de.saly.elasticsearch.importer.imap.support.DeleteByQuery;
import de.saly.elasticsearch.importer.imap.support.IndexableMailMessage;

public class ElasticsearchMailDestination implements MailDestination {

    private Client client;

    private volatile boolean closed;

    private volatile boolean error;

    private String index;

    private Map<String, Object> mapping;

    private Map<String, Object> settings;

    private volatile boolean started;
    
    private volatile boolean initialized;

    private boolean stripTagsFromTextContent = true;

    private String type;

    private boolean withAttachments = false;

    private boolean withTextContent = true;

    private boolean withHtmlContent = false;

    private boolean preferHtmlContent = false;

    private List<String> headersToFields;

    protected final ESLogger logger = ESLoggerFactory.getLogger(this.getClass().getName());

    @Override
    public void clearDataForFolder(final Folder folder) throws IOException, MessagingException {

        logger.info("Delete locally all messages for folder {} in {}/{}",folder.getURLName().toString(), index, type);

        createIndexIfNotExists();
        
        client.admin().indices().refresh(new RefreshRequest()).actionGet();
        
        DeleteByQuery.deleteByQuery(client, index, new String[]{type}, QueryBuilders.termQuery("folderUri", folder.getURLName().toString()));

    }

    public ElasticsearchMailDestination client(final Client client) {
        this.client = client;
        return this;
    }

    @Override
    public synchronized void close() {

        if (closed) {
            return;
        }

        closed = true;

        logger.info("Closed");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Set getCurrentlyStoredMessageUids(final Folder folder) throws IOException, MessagingException {

        createIndexIfNotExists();
        
        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        final Set uids = new HashSet();

        final TermQueryBuilder b = QueryBuilders.termQuery("folderUri", folder.getURLName().toString());

        logger.debug("Term query: " + b.buildAsBytes().toUtf8());

        SearchResponse scrollResp = client.prepareSearch().setIndices(index).setTypes(type).setSearchType(SearchType.SCAN).setQuery(b)
                .setScroll(new TimeValue(1000)).setSize(1000).execute().actionGet();

        while (true) {
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(1000)).execute().actionGet();
            boolean hitsRead = false;
            for (final SearchHit hit : scrollResp.getHits()) {
                hitsRead = true;

                if (folder instanceof IMAPFolder) {
                    uids.add(Long.parseLong(hit.getId().split("::")[0]));
                } else {
                    uids.add(hit.getId().split("::")[0]);
                }

                logger.debug("Local: " + hit.getId());
            }
            if (!hitsRead) {
                break;
            }
        }

        logger.debug("Currently locally stored messages for folder {}: {}", folder.getURLName(), uids.size());

        return uids;

    }

    @Override
    public int getFlaghashcode(final String id) throws IOException, MessagingException {

        createIndexIfNotExists();
        
        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        final GetResponse getResponse = client.prepareGet().setIndex(index).setType(type).setId(id)
                .setFields(new String[] { "flaghashcode" }).execute().actionGet();

        if (getResponse == null || !getResponse.isExists()) {
            return -1;
        }

        final GetField flaghashcodeField = getResponse.getField("flaghashcode");

        if (flaghashcodeField == null || flaghashcodeField.getValue() == null || !(flaghashcodeField.getValue() instanceof Number)) {
            throw new IOException("No flaghashcode field for id " + id+ " ("+(flaghashcodeField==null?"null":"Val: "+flaghashcodeField.getValue())+")");
        }

        return ((Number) flaghashcodeField.getValue()).intValue();

    }

    @Override
    public Set<String> getFolderNames() throws IOException, MessagingException {

        createIndexIfNotExists();
        
        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        final HashSet<String> uids = new HashSet<String>();

        SearchResponse scrollResp = client.prepareSearch().setIndices(index).setTypes(type).setSearchType(SearchType.SCAN)
                .setQuery(QueryBuilders.matchAllQuery()).addField("folderFullName").setScroll(new TimeValue(1000)).setSize(1000).execute()
                .actionGet();

        while (true) {
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(1000)).execute().actionGet();
            boolean hitsRead = false;
            for (final SearchHit hit : scrollResp.getHits()) {
                hitsRead = true;
                uids.add((String) hit.getFields().get("folderFullName").getValue());

            }
            if (!hitsRead) {
                break;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Currently locally stored folders: {}", uids);
        }

        return uids;

    }

    public List<String> getHeadersToFields() {
        return headersToFields;
    }

    public boolean isStripTagsFromTextContent() {
        return stripTagsFromTextContent;
    }

    public boolean isWithAttachments() {
        return withAttachments;
    }

    public boolean isWithTextContent() {
        return withTextContent;
    }

    public boolean isWithHtmlContent() {
        return withHtmlContent;
    }

    public boolean isPreferHtmlContent() {
        return preferHtmlContent;
    }

    @Override
    public void onMessage(final Message msg) throws IOException, MessagingException {
        if (closed) {
            if (logger.isTraceEnabled()) {
                logger.trace("Is closed, will not index");
            }
            return;
        }

        if (isError()) {
            if (logger.isTraceEnabled()) {
                logger.trace("error, not indexing");
            }
            return;
        }
        
        createIndexIfNotExists();

        final IndexableMailMessage imsg = IndexableMailMessage.fromJavaMailMessage(msg, withTextContent, withHtmlContent, preferHtmlContent, withAttachments,
                stripTagsFromTextContent, headersToFields);

        if (logger.isTraceEnabled()) {
            logger.trace("Process mail " + imsg.getUid() + "/" + imsg.getPopId() + " :: " + imsg.getSubject() + "/" + imsg.getSentDate());
        }

        client.index(createIndexRequest(imsg)).actionGet();

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onMessageDeletes(final Set msgs, final Folder folder) throws IOException, MessagingException {

        if (msgs.size() == 0) {
            return;
        }

        createIndexIfNotExists();
        
        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        logger.info("Will delete " + msgs.size() + " messages locally for folder " + folder.getURLName().toString());

        final BoolQueryBuilder query = new BoolQueryBuilder();

        if (folder instanceof POP3Folder) {
            query.must(QueryBuilders.termsQuery("popId", msgs));
        } else {
            query.must(QueryBuilders.termsQuery("uid", msgs));
        }

        query.must(QueryBuilders.termQuery("folderUri", folder.getURLName().toString()));

        DeleteByQuery.deleteByQuery(client, index, new String[]{type}, query);

    }

    public ElasticsearchMailDestination setIndex(final String index) {
        this.index = index;
        return this;
    }

    public ElasticsearchMailDestination setMapping(final Map<String, Object> mapping) {
        this.mapping = mapping;
        return this;
    }

    public ElasticsearchMailDestination setSettings(final Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    public ElasticsearchMailDestination setStripTagsFromTextContent(final boolean stripTagsFromTextContent) {
        this.stripTagsFromTextContent = stripTagsFromTextContent;
        return this;
    }

    public ElasticsearchMailDestination setType(final String type) {
        this.type = type;
        return this;
    }

    public ElasticsearchMailDestination setWithAttachments(final boolean withAttachments) {
        this.withAttachments = withAttachments;
        return this;
    }

    public ElasticsearchMailDestination setWithTextContent(final boolean withTextContent) {
        this.withTextContent = withTextContent;
        return this;
    }

    public ElasticsearchMailDestination setWithHtmlContent(final boolean withHtmlContent) {
        this.withHtmlContent = withHtmlContent;
        return this;
    }

    public ElasticsearchMailDestination setPreferHtmlContent(final boolean preferHtmlContent) {
        this.preferHtmlContent = preferHtmlContent;
        return this;
    }

    public MailDestination setHeadersToFields(List<String> headersToFields) {
        this.headersToFields = headersToFields;
        return this;
    }

    @Override
    public synchronized ElasticsearchMailDestination startup() throws IOException {
        
        if (started) {
            logger.debug("Destination already started");
            return this;
        }
        started = true;
        logger.debug("Destination started");
        return this;
    }

    private synchronized void createIndexIfNotExists() throws IOException {
        if (isError()) {
            if (logger.isTraceEnabled()) {
                logger.trace("error, not creating index");
            }
            return;
        }

        if(initialized) {
            return;
        }
        
        
        IMAPImporter.waitForYellowCluster(client);
        
        // create index if it doesn't already exist
        if (!client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
    
            final CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);
            if (settings != null) {
                logger.debug("index settings are provided, will apply them {}", settings);
                createIndexRequestBuilder.setSettings(settings);
            } else {
                logger.debug("no settings given for index '{}'",index);
            }
            
            
            if (mapping != null) {
                logger.warn("mapping for type '{}' is provided, will apply {}", type, mapping);
                createIndexRequestBuilder.addMapping(type, mapping);
            } else {
                logger.debug("no mapping given for type '{}', will apply default mapping",type);
                createIndexRequestBuilder.addMapping(type, getDefaultTypeMapping());
            }
            
            final CreateIndexResponse res = createIndexRequestBuilder.get();
            
            if (!res.isAcknowledged()) {
                throw new IOException("Could not create index " + index);
            }
            
            IMAPImporter.waitForYellowCluster(client);
            
            logger.info("Index {} created", index);
            
        } else {
            logger.debug("Index {} already exists", index);
        }
        
        initialized = true;
    }
    
    
    private XContentBuilder getDefaultTypeMapping() throws IOException {
        
        final XContentBuilder mappingBuilder = jsonBuilder().startObject().startObject(type).startObject("properties")
                .startObject("folderFullName").field("index", "not_analyzed").field("type", "string").endObject()
                .startObject("folderUri").field("index", "not_analyzed").field("type", "string").endObject()
                .startObject("contentType").field("index", "not_analyzed").field("type", "string").endObject()
                .startObject("receivedDate").field("type", "date").field("format", "basic_date_time").endObject()
                .startObject("sentDate").field("type", "date").field("format", "basic_date_time").endObject()
                .startObject("flaghashcode").field("type", "integer").endObject();
        
       /* "attachments":{
            "properties":{
               "content":{
                  "type":"attachment",
                  "fields":{
                     "content":{
                        "store": true,
                        "index": "analyzed"
                     },
                     "title" : {"store" : "yes"},
                     "content_type" : {"store" : "yes"}
                  }
               }
            }
         }*/
        
                if(withAttachments) {
                    logger.info("Configure Attachments Mapper Plugin");
                    mappingBuilder
                       .startObject("attachments")
                          .startObject("properties")
                             .startObject("content")
                               .field("type", "attachment")
                               .startObject("fields")
                                  .startObject("content")
                                   .field("store", true)
                                   .field("index", "analyzed")
                                   .endObject()
                                .startObject("title")
                                .field("store",true)
                                .endObject()
                                .startObject("content_type")
                                .field("store",true)
                                .endObject()
                             .endObject()
                          .endObject()
                       .endObject()
                    .endObject();
                }
                
                // .startObject("attachments").startObject("properties").startObject("content").field("type",
                // "attachment").endObject().endObject().endObject()
                mappingBuilder.endObject().endObject().endObject();

        return mappingBuilder;
        
    }

    protected IndexRequest createIndexRequest(final IndexableMailMessage message) throws IOException {

        final String id = (!StringUtils.isEmpty(message.getPopId()) ? message.getPopId() : message.getUid()) + "::"
                + message.getFolderUri();
        
        //if(logger.isTraceEnabled()) {
        //   logger.trace("Message: "+message.build());
        //}
        
        final IndexRequest request = Requests.indexRequest(index).type(type).id(id).source(message.build());

        return request;

    }

    protected Client getClient() {
        return client;
    }

    protected synchronized boolean isClosed() {
        return closed;
    }

    protected synchronized boolean isError() {
        return error;
    }

    protected void setError(final boolean error) {
        this.error = error;
    }
    
}
