package com.ava.frame.es;

import com.ava.frame.core.SpringApplicationContext;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by redredava on 2018/6/27.
 * email:zhyx2014@yeah.net
 */
public class EsQuery {
    private TransportClient client;
    private BoolQueryBuilder query;
    private int limit;
    private String index;
    private String type;

    private EsQuery() {
        EsClient esClient = SpringApplicationContext.getBean("esClient");
        client = esClient.getClient();
        index = esClient.getEsConfig().getIndex();
        type = esClient.getEsConfig().getType();
        query = QueryBuilders.boolQuery();
    }

    private EsQuery(String index, String type) {
        EsClient esClient = SpringApplicationContext.getBean("esClient");
        client = esClient.getClient();
        this.index = index;
        this.type = type;
        query = QueryBuilders.boolQuery();
    }

    private EsQuery(String type) {
        EsClient esClient = SpringApplicationContext.getBean("esClient");
        client = esClient.getClient();
        index = esClient.getEsConfig().getIndex();
        this.type = type;
        query = QueryBuilders.boolQuery();
    }

    public static EsQuery build() {
        return new EsQuery();
    }

    public static EsQuery build(String index, String type) {
        return new EsQuery(index, type);
    }

    public static EsQuery build(String type) {
        return new EsQuery(type);
    }

    public EsQuery term(String field, String value, Link link) {
        if (link == Link.SHOULD) query.should(new TermQueryBuilder(field, value));
        if (link == Link.MUST) query.must(new TermQueryBuilder(field, value));
        return this;
    }

    public EsQuery query(String field, String value, Link link) {
        if (link == Link.SHOULD) query.should(new QueryStringQueryBuilder(value).field(field));
        if (link == Link.MUST) query.must(new QueryStringQueryBuilder(value).field(field));
        return this;
    }

    public void updateIndex(String id, Map source) {
        IndexRequest indexRequest = new IndexRequest(index, type, id)
                .source(source);
        UpdateRequest updateRequest = new UpdateRequest(index, type, id)
                .doc(source)
                .upsert(indexRequest);
        try {
            client.update(updateRequest).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public EsQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    public EsDomain one() {
        try {
            return list().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public List<EsDomain> list() {
        try {
            SearchResponse response = client.prepareSearch(index).setTypes(type)
                    .setQuery(query)
                    .execute().get();
            SearchHits hits = response.getHits();
            List<EsDomain> list = new ArrayList<EsDomain>();
            for (SearchHit hit : hits) {
                list.add(new EsDomain(hit.getId(),hit.getScore(), hit.getSourceAsMap()));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public enum Link {
        MUST, SHOULD
    }
}
