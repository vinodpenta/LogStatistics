package com.home.logs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.home.logs.fancylog.utils.FancySharedInfo;

public class FancyLogSchedulerMain {
    public static void main(String[] args) {
        FancySharedInfo.getInstance().setLastTaskSuccessful(true);
        new ClassPathXmlApplicationContext("applicationContext.xml");
    }

}
