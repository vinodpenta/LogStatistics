package com.tcs.klm.fancylog.utils;

public class FancySharedInfo {

    private boolean isLastTaskSuccessful;
    private boolean isDownloadInProgress;
    private boolean isAnalysisInProgress;

    private static FancySharedInfo fancySharedInfo;

    private FancySharedInfo() {
        isLastTaskSuccessful = true;
        isDownloadInProgress = false;
        isAnalysisInProgress = false;
    }

    public boolean isDownloadInProgress() {
        return isDownloadInProgress;
    }

    public void setDownloadInProgress(boolean isDownloadInProgress) {
        this.isDownloadInProgress = isDownloadInProgress;
    }

    public boolean isAnalysisInProgress() {
        return isAnalysisInProgress;
    }

    public void setAnalysisInProgress(boolean isAnalysisInProgress) {
        this.isAnalysisInProgress = isAnalysisInProgress;
    }

    public static FancySharedInfo getInstance() {
        if (fancySharedInfo == null) {
            fancySharedInfo = new FancySharedInfo();
        }
        return fancySharedInfo;
    }

    public boolean isLastTaskSuccessful() {
        return isLastTaskSuccessful;
    }

    public void setLastTaskSuccessful(boolean isLastTaskSuccessful) {
        this.isLastTaskSuccessful = isLastTaskSuccessful;
    }

}
