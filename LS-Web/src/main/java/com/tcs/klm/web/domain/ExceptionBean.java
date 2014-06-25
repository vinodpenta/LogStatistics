package com.tcs.klm.web.domain;

public class ExceptionBean {

    private String className;
    private String exceptionDescription;
    private int count = 0;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getExceptionDescription() {
        return exceptionDescription;
    }

    public void setExceptionDescription(String exceptionDescription) {
        this.exceptionDescription = exceptionDescription;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

}
