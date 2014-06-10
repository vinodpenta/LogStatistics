package com.tcs.klm.fancylog.job;

import java.io.IOException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogAnalysisTask;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

public class FancyLogAnalysisJob extends QuartzJobBean {

    private FancyLogAnalysisTask fancyLogAnalysisTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (FancySharedInfo.getInstance().isLastTaskSuccessful()) {
            if (!FancySharedInfo.getInstance().isDownloadInProgress()) {
                try {
                    fancyLogAnalysisTask.performTask();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setFancyLogAnalysisTask(FancyLogAnalysisTask fancyLogAnalysisTask) {
        this.fancyLogAnalysisTask = fancyLogAnalysisTask;
    }

}
