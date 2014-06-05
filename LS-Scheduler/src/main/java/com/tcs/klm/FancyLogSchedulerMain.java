package com.tcs.klm;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tcs.klm.fancylog.utils.FancySharedInfo;

public class FancyLogSchedulerMain {
    public static void main(String[] args) {
        FancySharedInfo.getInstance().setLastTaskSuccessful(true);
        new ClassPathXmlApplicationContext("applicationContext.xml");
    }

}
