package com.tcs.klm.fancylog.task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.mongodb.BasicDBObject;
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
    private String COLLECTION_TRANSACTION = "transactions";
    private String COLLECTION_LOGS = "logs";

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
            List<String> lstHyeperLink = new ArrayList<String>();

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
                    starFileDownloadAndAnalysis(logInURL, userName, passWord, lstHyeperLink, sessionIDPossition);
                }
            }
            FancySharedInfo.getInstance().incrementCalenderByOneHr();
            Object value = settings.get("noOfDays");
            if (value != null) {
                int noOfDays = Integer.valueOf(value.toString());
                Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.HOUR_OF_DAY, -noOfDays * 24);
                Date today = calendar2.getTime();
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH");
                String date = formatter.format(today);
                DBCollection dbCollectionLog = mongoTemplate.getCollection(COLLECTION_LOGS);
                BasicDBObject searchQuery = new BasicDBObject();
                searchQuery.put("date", date);
                dbCollectionLog.remove(searchQuery);

                BasicDBObject regexQuery = new BasicDBObject();
                regexQuery.put("date", new BasicDBObject("$regex", date + ".*").append("$options", "i"));

                DBCollection dbCollectionTransaction = mongoTemplate.getCollection(COLLECTION_TRANSACTION);
                dbCollectionTransaction.remove(regexQuery);
            }
        }
    }

    private void starFileDownloadAndAnalysis(String logInURL, String userName, String passWord, List<String> lstHyeperLink, String sessionIDPossition) {
        APPLICATION_LOGGER.info("Download analysis Started... ");
        try {
            Runnable task;
            List<Thread> threads = new ArrayList<Thread>();
            for (String hyperLink : lstHyeperLink) {
                task = new DownloadAnalysisThread(logInURL, userName, passWord, hyperLink, sessionIDPossition, logAnalyzerMap, mongoTemplate);
                Thread thread = new Thread(task);
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.join();
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

}
