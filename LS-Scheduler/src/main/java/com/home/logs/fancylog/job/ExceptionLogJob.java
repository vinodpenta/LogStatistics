package com.home.logs.fancylog.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.home.logs.fancylog.task.ExceptionLogTask;

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
