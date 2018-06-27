package com.ava.frame.es;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by redredava on 2018/6/27.
 * email:zhyx2014@yeah.net
 */
@Component
public class EsClient implements InitializingBean {
    private TransportClient client;
    @Autowired
    private EsConfig esConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        Settings settings = Settings.builder()
                .put("cluster.name", esConfig.getClusterName())
                .put("client.transport.sniff", true)
//                .put("node.name", esConfig.getNodeName())
                .build();
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(
                            esConfig.getIp()),
                            esConfig.getPort()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void createEmptyIndex() {
        createEmptyIndex(esConfig.getIndex());
    }

    public void createEmptyIndex(String index) {
        client.admin().indices().prepareCreate(index).execute().actionGet();  //创建一个空索引，如没有索引，创建mapping时会报错
    }

    public void mapping() {
        mapping(esConfig.getIndex(), esConfig.getType());
    }

    public void mapping(String index, String type) {
        try {
            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(type)
                    .startObject("properties")
                    .startObject("domain").field("type", "keyword").endObject()
                    .startObject("words").field("type", "text").field("store", "true").field("analyzer", "ik_smart_syn").field("search_analyzer", "ik_smart_syn").endObject()
                    .startObject("uuid").field("type", "keyword").field("index", "false").field("store", "true").endObject()
                    .startObject("device_name").field("type", "keyword").field("index", "false").field("store", "true").endObject()
                    .startObject("stream_id").field("type", "keyword").field("index", "false").field("store", "true").endObject()
                    .startObject("type").field("type", "keyword").field("index", "false").field("store", "true").endObject()
                    .startObject("absolute").field("type", "boolean").field("index", "false").field("store", "true").endObject()
                    .startObject("t").field("type", "text").field("index", "false").field("store", "true").endObject()
                    .startObject("v").field("type", "long").field("index", "false").field("store", "true").endObject()
                    .startObject("score").field("type", "long").field("index", "false").field("store", "true").endObject()
                    .endObject().endObject().endObject();
            PutMappingRequest mappingRequest = Requests.putMappingRequest(index).type(type).source(mapping);
            client.admin().indices().putMapping(mappingRequest).actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void mapping1() {
        mapping1(esConfig.getIndex(), esConfig.getType());
    }
    public void mapping1(String index, String type) {
        try {
            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(type)
                    .startObject("properties")
                    .startObject("domain").field("type", "keyword").endObject()
                    .startObject("words").field("type", "text").field("store", "true").field("analyzer", "ik_smart_syn").field("search_analyzer", "ik_smart_syn").endObject()
                    .startObject("uuid").field("type", "keyword").field("index", "false").field("store", "true").endObject()
                    .startObject("param").field("type", "text").field("index", "false").field("store", "true").endObject()
                    .startObject("score").field("type", "long").field("index", "false").field("store", "true").endObject()
                    .endObject().endObject().endObject();
            PutMappingRequest mappingRequest = Requests.putMappingRequest(index).type(type).source(mapping);
            client.admin().indices().putMapping(mappingRequest).actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String createIndex(String id, Map<String, Object> source) {
        return createIndex(esConfig.getIndex(), esConfig.getType(), id, source);
    }

    public String createIndex(String index, String type, String id, Map<String, Object> source) {
        IndexResponse response = client.prepareIndex(index, type).setSource(source).get();
        return response.getIndex();
    }

    public TransportClient getClient() {
        return client;
    }

    public EsConfig getEsConfig() {
        return esConfig;
    }

    public void setEsConfig(EsConfig esConfig) {
        this.esConfig = esConfig;
    }


}
