package com.tcs.klm.domain;

public class SettingsBean {

    private String id;
    private String ApplicationName;
    private String fancyLogURLPattern;
    private String host;
    private String nodeList;
    private String instance;
    private String sessionIdPosition;
    private String noOfDays;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public String getFancyLogURLPattern() {
        return fancyLogURLPattern;
    }

    public void setFancyLogURLPattern(String fancyLogURLPattern) {
        this.fancyLogURLPattern = fancyLogURLPattern;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNodeList() {
        return nodeList;
    }

    public void setNodeList(String nodeList) {
        this.nodeList = nodeList;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getSessionIdPosition() {
        return sessionIdPosition;
    }

    public void setSessionIdPosition(String sessionIdPosition) {
        this.sessionIdPosition = sessionIdPosition;
    }

    public String getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(String noOfDays) {
        this.noOfDays = noOfDays;
    }
}
