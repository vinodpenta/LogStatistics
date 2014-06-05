package com.tcs.klm.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogDownloadTask;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

public class FancyLogDownloadJob extends QuartzJobBean {

    @Autowired
    private FancyLogDownloadTask fancyLogDownloadTask;

    public void setFancyLogDownloadTask(FancyLogDownloadTask fancyLogDownloadTask) {
        this.fancyLogDownloadTask = fancyLogDownloadTask;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (FancySharedInfo.getInstance().isLastTaskSuccessful()) {
            if (!FancySharedInfo.getInstance().isAnalysisInProgress()) {
                fancyLogDownloadTask.performTask();
            }
        }

    }
}
