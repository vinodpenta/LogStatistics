package com.tcs.klm.fancylog.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
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
import com.tcs.klm.fancylog.thread.AnalysisThread;
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
            String downloadLocation = (String) settings.get("downloadLocation");
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
                    startFileDownload(lstHyeperLink, downloadLocation, httpClient);
                    startFileAnalysis(downloadLocation, sessionIDPossition);
                    // starFileDownloadAndAnalysis(logInURL, userName, passWord, lstHyeperLink, sessionIDPossition, downloadLocation);
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

    private void startFileAnalysis(String downloadLocation, String sessionIDPossition) {
        File[] files = getListOfFiles(downloadLocation);
        if (files != null) {
            try {
                Runnable task;
                List<Thread> threads = new ArrayList<Thread>();
                for (File file : files) {
                    task = new AnalysisThread(file, sessionIDPossition, logAnalyzerMap, mongoTemplate);
                    Thread thread = new Thread(task);
                    thread.start();
                    threads.add(thread);
                }
                for (Thread thread : threads) {
                    thread.join();
                }

            }
            catch (Exception e) {
                APPLICATION_LOGGER.error("", e);
            }

        }
    }

    private void startFileDownload(List<String> lstHyeperLink, String downloadLocation, HttpClient httpClient) {

        boolean downloadSuccessFlag = false;
        APPLICATION_LOGGER.info("Download Started...");
        for (String hyperLink : lstHyeperLink) {
            GetMethod getMethodLog = new GetMethod(hyperLink);
            try {
                int code = httpClient.executeMethod(getMethodLog);
                if (code == 200) {
                    APPLICATION_LOGGER.info("response code 200");
                    (new File(downloadLocation)).mkdirs();
                    int fileNameBeginIndex = hyperLink.indexOf("oldlogs/") + "oldlogs/".length();
                    int fileNameEndIndex = hyperLink.indexOf("&app=");
                    String fileName = downloadLocation + hyperLink.substring(fileNameBeginIndex, fileNameEndIndex);
                    fileName = fileName.replace(".gz", ".log");
                    BufferedInputStream isTextOrTail = new BufferedInputStream(getMethodLog.getResponseBodyAsStream());
                    downloadFileContent(isTextOrTail, fileName);
                    APPLICATION_LOGGER.info(hyperLink);

                }
                else {
                    APPLICATION_LOGGER.info("failed to download log file " + hyperLink);
                    APPLICATION_LOGGER.info("Http Status Code : " + code);
                }
            }
            catch (HttpException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        APPLICATION_LOGGER.info("Download Completed...  >> " + downloadSuccessFlag);
    }

    private void downloadFileContent(BufferedInputStream isTextOrTail, String fileName) {
        APPLICATION_LOGGER.info("Downloading file {}", fileName);
        GZIPInputStream gzis = null;
        OutputStream out = null;
        try {
            File targetFile = new File(fileName);
            gzis = new GZIPInputStream(isTextOrTail);
            out = new FileOutputStream(targetFile);
            IOUtils.copy(isTextOrTail, out);
            APPLICATION_LOGGER.info("Downloading is finished file {}", fileName);
        }
        catch (Exception ex) {
            APPLICATION_LOGGER.error("Exception in downloadAnalysisThread {}", ex);
        }
        finally {
            if (isTextOrTail != null) {
                try {
                    isTextOrTail.close();
                    gzis.close();

                }
                catch (Exception e) {
                    APPLICATION_LOGGER.error("" + e);
                }
            }
        }

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

    private static File[] getListOfFiles(String directoryPath) {
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }

}
