package com.home.logs.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.home.logs.fancylog.task.FancyLogDownloadTask;
import com.home.logs.fancylog.utils.FancySharedInfo;

public class FancyLogDownloadJob extends QuartzJobBean {
    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(FancyLogDownloadJob.class);

    private FancyLogDownloadTask fancyLogDownloadTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            APPLICATION_LOGGER.info("FancyLogDownloadJob");
            if (FancySharedInfo.getInstance().isLastTaskSuccessful()) {
                if (!FancySharedInfo.getInstance().isAnalysisInProgress()) {
                    fancyLogDownloadTask.performTask();
                }
            }
        }
        catch (Exception exception) {
            APPLICATION_LOGGER.equals(exception.getStackTrace());
        }

    }

    public void setFancyLogDownloadTask(FancyLogDownloadTask fancyLogDownloadTask) {
        this.fancyLogDownloadTask = fancyLogDownloadTask;
    }
}
