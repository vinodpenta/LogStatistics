package com.tcs.klm.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogDownloadTask;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

public class FancyLogDownloadJob extends QuartzJobBean {

    private FancyLogDownloadTask fancyLogDownloadTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            if (FancySharedInfo.getInstance().isLastTaskSuccessful()) {
                if (!FancySharedInfo.getInstance().isAnalysisInProgress()) {
                    fancyLogDownloadTask.performTask();
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void setFancyLogDownloadTask(FancyLogDownloadTask fancyLogDownloadTask) {
        this.fancyLogDownloadTask = fancyLogDownloadTask;
    }
}
