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
package de.saly.elasticsearch.importer.imap;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.mapper.attachments.MapperAttachmentsPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.PluginAwareNode;

import com.google.common.collect.Lists;

import de.saly.elasticsearch.importer.imap.impl.IMAPImporter;

public class IMAPImporterCl {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static IMAPImporter imap;
    private static Node node;
    private static Client client;
    
    public static void main(String[] args) throws Exception {
        
        if(args.length < 1) {
            System.out.println("Usage: IMAPImporterC [-e] <config-file>");
            System.exit(-1);
        }
        
        String configFile = null;
        boolean embedded = false;
        
        if(args.length == 1) {
            configFile = args[0];
        }
        
        if(args.length == 2) {
            
            embedded = "-e".equals(args[0]);
            configFile = args[1];
        }
        
        System.out.println("Config File: "+configFile);
        System.out.println("Embedded: "+embedded);
        
        final Thread mainThread = Thread.currentThread();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                
                System.out.println("Will shutdown ...");
                
                IMAPImporterCl.stop();
                
                try {
                    mainThread.join();
                    System.out.println("Shudown done");
                } catch (InterruptedException e) {

                }
            }
        });
        
        start(configFile, embedded);
    }
    
    public static void stop() {
        
        if(imap != null) {
            imap.close();
        }
        
        if(client != null) {
            client.close();
        }
        
        if(node != null) {
            node.close();
        }
    }
    
    public static void start(Map<String, Object> settings, boolean embeddedMode) throws Exception {

        Settings.Builder builder = Settings.settingsBuilder();

        for(String key: settings.keySet()) {
            builder.put(key, String.valueOf(settings.get(key)));
        }
        
        if(embeddedMode) {
            try {
                FileUtils.forceDelete(new File("./data"));
            } catch (Exception e) {
                //ignore
            }
            builder.put("path.home",".");
            builder.put("node.local", true);
            builder.put("http.cors.enabled", true);
            builder.put("http.cors.allow-origin", "*");
            builder.put("cluster.name", "imap-embedded-"+System.currentTimeMillis());
            node = new PluginAwareNode(builder.build(), (Collection) Lists.newArrayList(MapperAttachmentsPlugin.class));
            node.start();
            client = node.client();
        }else
        {
            Settings eSettings = builder.build();
            client = new TransportClient.Builder().settings(eSettings).build();
            String[] hosts = eSettings.get("elasticsearch.hosts").split(",");
            
            for (int i = 0; i < hosts.length; i++) {
                String host = hosts[i];
                String hostOnly = host.split(":")[0];
                String portOnly = host.split(":")[1];
                System.out.println("Adding "+hostOnly+":"+portOnly);
                ((TransportClient)client).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostOnly), Integer.parseInt(portOnly)));
            }
        }
        imap = new IMAPImporter(settings, client);
        imap.start();
    }
    
    @SuppressWarnings("unchecked")
    public static void start(String configFile, boolean embeddedMode) throws Exception {
        Map<String, Object> settings = MAPPER.readValue(new File(configFile), Map.class);
        start(settings, embeddedMode);
    }

}
