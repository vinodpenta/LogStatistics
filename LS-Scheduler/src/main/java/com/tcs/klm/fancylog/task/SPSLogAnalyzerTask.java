package com.tcs.klm.fancylog.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tcs.klm.fancylog.analysis.LogAnalyzer;
import com.tcs.klm.fancylog.thread.DownloadAnalysisThread;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

@Component
public class SPSLogAnalyzerTask {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(SPSLogAnalyzerTask.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Map<String, LogAnalyzer> logAnalyzerMap;

    public static final String COLLECTION_NAME = "settings";

    public void performTask(Calendar calendar) {
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_NAME);
        DBCursor dbCursor = dbCollection.find();
        while (dbCursor.hasNext()) {
            DBObject settings = dbCursor.next();
            String applicationName = (String) settings.get("applicationName");
            String fancyLogURLPattern = (String) settings.get("fancyLogURLPattern");
            String host = (String) settings.get("host");
            String nodeList = (String) settings.get("nodeList");
            String[] nodes = nodeList.split(",");
            String instance = (String) settings.get("instance");
            String logInURL = (String) settings.get("logInURL");
            String userName = (String) settings.get("userName");
            String passWord = (String) settings.get("passWord");
            String fileName = (String) settings.get("fileName");
            String sessionIDPossition = (String) settings.get("sessionIdPosition");
            String downloadLocation = (String) settings.get("downloadLocation");
            String noOfDays = (String) settings.get("noOfDays");
            List<String> lstHyeperLink = new ArrayList<String>();
            APPLICATION_LOGGER.info("trying to loggin Fancylog main page");
            HttpClient httpClient = FancySharedInfo.getInstance().getAuthenticatedHttpClient(logInURL, userName, passWord);

            if (httpClient != null) {
                String date = FancySharedInfo.getInstance().getDateFormat(calendar);
                String logFileURL = null;
                fancyLogURLPattern = fancyLogURLPattern.replace("<host>", host);
                fancyLogURLPattern = fancyLogURLPattern.replace("<instance>", instance);
                fancyLogURLPattern = fancyLogURLPattern.replace("<applicationName>", applicationName);
                for (String node : nodes) {
                    String fancyLogURL = fancyLogURLPattern.replace("<node>", node);
                    String responseStream = getFancyLogMainPage(httpClient, fancyLogURL);
                    if (responseStream != null) {
                        Pattern regexPattern = Pattern.compile("<a\\s[^>]*href\\s*=\\s*\\\"([^\"]*)\"[^>]*>(.*?)</a>");
                        Matcher matcher = regexPattern.matcher(responseStream);
                        while (matcher.find()) {
                            logFileURL = matcher.group(1);
                            if (isValid(logFileURL, fileName, date)) {
                                lstHyeperLink.add(logFileURL);
                            }
                        }
                    }
                }
                if (!lstHyeperLink.isEmpty()) {
                    starFileDownloadAndAnalysis(logInURL, userName, passWord, lstHyeperLink, sessionIDPossition, downloadLocation, noOfDays);
                }
                File file = new File(downloadLocation);
                if (FancySharedInfo.getInstance().getFaildHyperLinks() != null) {
                    APPLICATION_LOGGER.info("retrying failed log files");
                    deleteDirectory(file);
                    starFileDownloadAndAnalysis(logInURL, userName, passWord, FancySharedInfo.getInstance().getFaildHyperLinks(), sessionIDPossition, downloadLocation, noOfDays);
                    FancySharedInfo.getInstance().clearFaildHyperLinks();
                }
                deleteDirectory(file);
            }
            FancySharedInfo.getInstance().incrementCalenderByOneHr();
        }
    }

    private void starFileDownloadAndAnalysis(String logInURL, String userName, String passWord, List<String> lstHyeperLink, String sessionIDPossition, String downloadLocation, String noOfDays) {
        APPLICATION_LOGGER.info("Download analysis Started... ");
        try {
            Runnable task;
            List<Thread> threads = new ArrayList<Thread>();
            for (String hyperLink : lstHyeperLink) {
                task = new DownloadAnalysisThread(logInURL, userName, passWord, hyperLink, sessionIDPossition, downloadLocation, logAnalyzerMap, mongoTemplate, noOfDays);
                Thread thread = new Thread(task);
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.join(60 * 60 * 1000);
            }
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        APPLICATION_LOGGER.info("Download analysis Completed...   ");
    }

    private boolean isValid(String logFileURL, String fileName, String date) {
        boolean flag = false;
        flag = (logFileURL.contains(".gz") || logFileURL.contains(".zip")) && logFileURL.contains("action=redir") && logFileURL.contains("oldlogs") && logFileURL.contains(fileName) && logFileURL.contains(date);
        return flag;
    }

    private String getFancyLogMainPage(HttpClient httpClient, String strFancyLogMainURL) {
        String responseString = null;
        GetMethod getMethod = new GetMethod(strFancyLogMainURL);
        int code = 0;
        try {
            code = httpClient.executeMethod(getMethod);
            responseString = getMethod.getResponseBodyAsString();
        }
        catch (HttpException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (code != 200) {
            APPLICATION_LOGGER.error("unable to access fancy log main page");
            return null;
        }
        else {
            return responseString;
        }
    }

    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

}
