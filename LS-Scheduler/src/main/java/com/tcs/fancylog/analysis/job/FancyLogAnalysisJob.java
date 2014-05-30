package com.tcs.fancylog.analysis.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.tcs.klm.fancylog.task.FancyLogAnalysisTask;

public class FancyLogAnalysisJob extends QuartzJobBean {

    @Autowired
    private FancyLogAnalysisTask fancyLogAnalysisTask;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("FancyLogAnalysisJob");

    }

    /*
     * public static void main(String ar[]) { System.out.println(System.getProperty("user.dir")); }
     */

}
