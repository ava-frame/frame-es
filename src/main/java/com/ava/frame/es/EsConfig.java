package com.ava.frame.es;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by redredava on 2018/6/27.
 * email:zhyx2014@yeah.net
 */
@Component
public class EsConfig {
    @Value("${es.ip}")
    private String ip;
    @Value("${es.port}")
    private int port;
    @Value("${es.cluster.name:semantic}")
    private String clusterName;
    @Value("${es.node.name:node-1}")
    private String nodeName;
    @Value("${es.index:search}")
    private String index;
    @Value("${es.type:SmartHome}")
    private String type;//    ==domain


    public EsConfig() {
        ip = "10.9.46.102";
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
