package com.tcs.klm.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogDownloadTask;

public class FancyLogDownloadJob extends QuartzJobBean {

    @Autowired
    private FancyLogDownloadTask fancyLogDownloadTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("FancyLogDownloadJob");

    }
}
