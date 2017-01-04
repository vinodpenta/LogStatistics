package com.home.logs.web.domain;

public class ExceptionBean {

    private String objectId;
    private String className;
    private String exception;
    private int count = 1;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

}
