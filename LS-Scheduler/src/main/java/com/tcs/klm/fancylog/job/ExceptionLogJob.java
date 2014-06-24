package com.tcs.klm.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.ExceptionLogTask;

public class ExceptionLogJob extends QuartzJobBean {

    @Autowired
    private ExceptionLogTask exceptionLogTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        exceptionLogTask.perfoemTask();
    }

    public void setExceptionLogTask(ExceptionLogTask exceptionLogTask) {
        this.exceptionLogTask = exceptionLogTask;
    }

}
