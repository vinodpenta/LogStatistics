package com.tcs.klm.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogAnalysisTask;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

public class FancyLogAnalysisJob extends QuartzJobBean {

    private FancyLogAnalysisTask fancyLogAnalysisTask;

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(FancyLogAnalysisJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (FancySharedInfo.getInstance().isLastTaskSuccessful()) {
            if (!FancySharedInfo.getInstance().isDownloadInProgress()) {
                try {
                    fancyLogAnalysisTask.performTask();
                }
                catch (Exception e) {
                    APPLICATION_LOGGER.error(e.toString());
                }
            }
        }
    }

    public void setFancyLogAnalysisTask(FancyLogAnalysisTask fancyLogAnalysisTask) {
        this.fancyLogAnalysisTask = fancyLogAnalysisTask;
    }

}
