package com.tcs.klm.web.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "settings")
public class SettingsBean {

    @Id
    private String id;
    private String applicationName;
    private String fancyLogURLPattern;
    private String host;
    private String nodeList;
    private String instance;
    private String sessionIdPosition;
    private String noOfDays;
    private String logInURL;
    private String userName;
    private String passWord;
    private String fileName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public String getLogInURL() {
        return logInURL;
    }

    public void setLogInURL(String logInURL) {
        this.logInURL = logInURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
