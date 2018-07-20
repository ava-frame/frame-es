package com.ava.frame.es;

import com.ava.frame.core.SpringApplicationContext;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.ArrayList;
import java.util.HashMap;
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
    private int limit = 5;//默认条数
    private String index;
    private String type;

    //    private String[] highField={"words"};
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

    /**
     * 分词
     *
     * @throws Exception
     */
    public List<AnalyzeResponse.AnalyzeToken> analyze(String text) throws Exception {
        return analyze(text, "ik_smart_syn");
    }

    /**
     * 分词
     *
     * @throws Exception
     */
    public List<AnalyzeResponse.AnalyzeToken> analyze(String text, String analyze, String... filters) throws Exception {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest(index);
        analyzeRequest.text(text);
        /**
         * whitespace （空白字符）分词器按空白字符 —— 空格、tabs、换行符等等进行简单拆分
         * letter 分词器 ，采用另外一种策略，按照任何非字符进行拆分
         * standard 分词器使用 Unicode 文本分割算法
         */
        analyzeRequest.analyzer(analyze);
        for (String filter : filters) {
            analyzeRequest.addTokenFilter(filter);
        }
//        analyzeRequest.addTokenFilter("standard");
//        analyzeRequest.addCharFilter("asciifolding");
        ActionFuture<AnalyzeResponse> analyzeResponseActionFuture = client.admin().indices().analyze(analyzeRequest);
        return analyzeResponseActionFuture.actionGet().getTokens();
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

    public void createEmptyIndex() {
        client.admin().indices().prepareCreate(index).execute().actionGet();  //创建一个空索引，如没有索引，创建mapping时会报错
    }

    public String createIndex(String id, Map<String, Object> source) {
        IndexResponse response = client.prepareIndex(index, type, id).setSource(source).get();
        return response.getIndex();
    }

    /**
     * 删除整个索引库
     *
     * @return
     */
    public boolean delIndex() {
        try {
            DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index)
                    .execute().actionGet();
            return dResponse.isAcknowledged();
        } catch (Exception e) {
//            index不存在时异常
            return false;
        }
    }

    /**
     * index 存在
     * @return
     */
    public boolean hasIndex() {
        IndicesExistsResponse inExistsResponse = client.admin().indices()
                .exists(new IndicesExistsRequest(index)).actionGet();
        return inExistsResponse.isExists();
    }

    /**
     *
     */
    public void delIndexQuery() {
        DeleteByQueryAction.INSTANCE.newRequestBuilder(client).source(index).filter(query).get();
    }

    /**
     * 通过ID删除
     *
     * @param id
     * @return
     */
    public boolean delIndexId(String id) {

        DeleteResponse dResponse = client.prepareDelete(index, type, id).execute().actionGet();
        if (dResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) return false;
        return true;
    }

    public EsQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 返回第一条
     * }
     */
    public EsDomain one() {
        try {
            return list().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 搜索前limit条数据
     *
     * @return
     */
    public List<EsDomain> list() {
        try {
            SearchResponse response = client.prepareSearch(index).setTypes(type)
                    .setQuery(query).setFrom(0).setSize(limit)
                    .execute().get();
            SearchHits hits = response.getHits();
            List<EsDomain> list = new ArrayList<EsDomain>();
            for (SearchHit hit : hits) {
                EsDomain esDomain = new EsDomain(hit.getId(), hit.getScore(), hit.getSourceAsMap());
                list.add(esDomain);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 返回结果中加入：某个字段的分词结果
     *
     * @param highlightField
     * @return
     */
    public List<EsDomain> list(String highlightField) {
        try {
            HighlightBuilder highlightBuilder = new HighlightBuilder().field(highlightField).requireFieldMatch(false);
            highlightBuilder.preTags(",");
            highlightBuilder.postTags(",");
            SearchResponse response = client.prepareSearch(index).setTypes(type)
                    .setQuery(query).setFrom(0).setSize(limit)
                    .highlighter(highlightBuilder)
                    .execute().get();
            SearchHits hits = response.getHits();
            List<EsDomain> list = new ArrayList<EsDomain>();
            for (SearchHit hit : hits) {
                EsDomain esDomain = new EsDomain(hit.getId(), hit.getScore(), hit.getSourceAsMap());
                Map<String, String> highs = new HashMap<String, String>(1);
                for (HighlightField field : hit.getHighlightFields().values()) {
                    if (highlightField.equalsIgnoreCase(field.getName()))
                        highs.put(field.getName(), field.getFragments()[0].string());
                }
                esDomain.setHighlights(highs);
                list.add(esDomain);
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
