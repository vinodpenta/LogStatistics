package com.home.logs.fancylog.job;

import java.util.Calendar;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.home.logs.fancylog.task.SPSLogAnalyzerTask;
import com.home.logs.fancylog.utils.FancySharedInfo;

public class SPSLogAnalyzerJob extends QuartzJobBean implements StatefulJob {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(SPSLogAnalyzerJob.class);

    @Autowired
    private SPSLogAnalyzerTask spsLogAnalyzerTask;

    public void setSpsLogAnalyzerTask(SPSLogAnalyzerTask spsLogAnalyzerTask) {
        this.spsLogAnalyzerTask = spsLogAnalyzerTask;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        APPLICATION_LOGGER.info("SPSLogAnalyzerJob started");
        Calendar calendar = null;
        if (FancySharedInfo.getInstance().getCalendar() == null) {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -2);
            FancySharedInfo.getInstance().setCalendar(calendar);
            spsLogAnalyzerTask.performTask(calendar);
        }
        else {
            calendar = FancySharedInfo.getInstance().getCalendar();
            spsLogAnalyzerTask.performTask(calendar);
        }
        APPLICATION_LOGGER.info("SPSLogAnalyzerJob end");
    }
}
