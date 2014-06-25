package com.tcs.klm.domain;


public class ExceptionBean {

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

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

}
