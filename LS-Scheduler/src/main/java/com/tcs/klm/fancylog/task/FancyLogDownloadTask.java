package com.tcs.klm.fancylog.task;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FancyLogDownloadTask {
    @Autowired
    private Properties environmentProperties;

    public void task() {
        System.out.println("123");
    }

    /*
     * private static File[] getListOfFiles(String directoryPath) { File folder = new File(directoryPath); File[] listOfFiles = folder.listFiles(); return listOfFiles; }
     */
}
