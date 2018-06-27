package com.ava.frame.es;

import java.util.Map;

/**
 * Created by redredava on 2018/6/27.
 * email:zhyx2014@yeah.net
 */
public class EsDomain {
    private String id;
    private float score;
    private Map<String,Object> source;

    public EsDomain(String id, float score, Map<String, Object> source) {
        this.id = id;
        this.score = score;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EsDomain(float score, Map<String, Object> source) {
        this.score = score;
        this.source = source;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

}
