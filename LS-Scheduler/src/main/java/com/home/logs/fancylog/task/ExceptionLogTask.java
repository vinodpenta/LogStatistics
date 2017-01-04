package com.home.logs.fancylog.task;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.home.logs.domain.ExceptionBean;
import com.home.logs.fancylog.thread.DownloadThread;
import com.home.logs.fancylog.thread.ExceptionAnalysisThread;

public class ExceptionLogTask {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(ExceptionLogTask.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String COLLECTION_SETTINGS = "settings";
    private String COLLECTION_EXCEPTION = "exception";

    public void perfoemTask() {
        APPLICATION_LOGGER.info("ExceptionLogTask started...");
        DBCollection settingsCollection = mongoTemplate.getCollection(COLLECTION_SETTINGS);
        DBCursor settingsCursor = settingsCollection.find();
        while (settingsCursor.hasNext()) {
            DBObject settings = settingsCursor.next();
            String sessionIDPossition = (String) settings.get("sessionIdPosition");
            String applicationName = (String) settings.get("applicationName");
            String fancyLogURLPattern = (String) settings.get("fancyLogURLPattern");
            String host = (String) settings.get("host");
            String nodeList = (String) settings.get("nodeList");
            String[] nodes = nodeList.split(",");
            String instance = (String) settings.get("instance");
            String logInURL = (String) settings.get("logInURL");
            String userName = (String) settings.get("userName");
            String passWord = (String) settings.get("passWord");
            String fileName = (String) settings.get("exceptionFiles");
            String fileNames[] = StringUtils.split(fileName, ",");
            String downloadLocation = (String) settings.get("downloadLocation");
            String[] names = StringUtils.split(downloadLocation, "/");
            String exceptionFileLocation = downloadLocation.replace(names[names.length - 1], "exception");
            List<String> lstHyeperLink = new ArrayList<String>();
            System.out.println(exceptionFileLocation);
            HttpClient httpClient = getAuthenticatedHttpClient(logInURL, userName, passWord);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar1.getTime();
            // needs to change date format
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(yesterday);
            System.out.println(date);
            String logFileURL = null;
            if (httpClient != null) {
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
                            if (isValid(logFileURL, fileNames, date)) {
                                lstHyeperLink.add(logFileURL);
                            }
                        }

                    }
                }
                starFileDownload(logInURL, userName, passWord, lstHyeperLink, exceptionFileLocation);
                File[] exceptionFiles = getListOfFiles(exceptionFileLocation);
                if (exceptionFiles != null) {
                    try {
                        Runnable task;
                        List<Thread> threads = new ArrayList<Thread>();
                        for (File file : exceptionFiles) {
                            task = new ExceptionAnalysisThread(file, sessionIDPossition, mongoTemplate);
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
                    File gzfolder = new File(exceptionFileLocation);
                    FancyLogAnalysisTask.deleteDirectory(gzfolder);
                }

            }
            APPLICATION_LOGGER.info("Exception log download completed.. ");
            Map<String, ExceptionBean> exceptionBeanMap = new HashMap<String, ExceptionBean>();
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("date", date);
            DBCollection collection = mongoTemplate.getCollection(COLLECTION_EXCEPTION);
            DBCursor cursor = collection.find(searchQuery);
            while (cursor.hasNext()) {
                String key;
                DBObject object = cursor.next();
                String className = (String) object.get("className");
                String exception = null;
                Object value = object.get("exception");
                if (value != null) {
                    exception = value.toString();
                }
                key = className + exception;
                ExceptionBean exceptionBean = exceptionBeanMap.get(key);
                if (exceptionBean != null) {
                    exceptionBean.incrementCount();
                }
                else {
                    ExceptionBean exceptionBean1 = new ExceptionBean();
                    exceptionBean1.setClassName(className);
                    exceptionBean1.setException(exception);
                    exceptionBean1.setDate(date);
                    exceptionBeanMap.put(key, exceptionBean1);
                }
            }

            List<ExceptionBean> exceptionBeans = new ArrayList<ExceptionBean>();
            Set<String> keySet = exceptionBeanMap.keySet();
            if (keySet != null) {
                for (String key : keySet) {
                    exceptionBeans.add(exceptionBeanMap.get(key));
                }
            }
            mongoTemplate.insertAll(exceptionBeans);
            APPLICATION_LOGGER.info("ExceptionBean update completed...");

        }
    }

    private static File[] getListOfFiles(String directoryPath) {
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }

    private boolean starFileDownload(String logInURL, String userName, String passWord, List<String> listHyeperLink, String downloadLocation) {
        boolean downloadSuccessFlag = false;
        if (listHyeperLink != null && !listHyeperLink.isEmpty()) {
            try {
                Runnable task;
                APPLICATION_LOGGER.info("Download Started... ");
                List<Thread> threads = new ArrayList<Thread>();
                /*
                 * ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor(); taskExecutor.setCorePoolSize(4); taskExecutor.setMaxPoolSize(20); taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
                 */
                for (String hyperLink : listHyeperLink) {
                    task = new DownloadThread(logInURL, userName, passWord, hyperLink, downloadLocation);
                    Thread thread = new Thread(task);
                    thread.start();
                    threads.add(thread);
                    // taskExecutor.execute(task);
                }
                for (Thread thread : threads) {
                    thread.join();
                }
                APPLICATION_LOGGER.info("Download ended... ");
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                APPLICATION_LOGGER.error("", e);
            }
        }
        APPLICATION_LOGGER.info("Download Completed...  >> {} ", System.currentTimeMillis());
        return downloadSuccessFlag;
    }

    private boolean isValid(String logFileURL, String[] fileNames, String date) {
        boolean flag = false;
        flag = (logFileURL.contains(".gz") || logFileURL.contains(".zip")) && logFileURL.contains("action=redir") && logFileURL.contains("oldlogs") && logFileURL.contains(date);
        if (flag && fileNames != null) {
            for (String fileName : fileNames) {
                flag = logFileURL.contains(fileName);
                if (flag)
                    return true;
            }

        }
        else {
            flag = false;
        }
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
            APPLICATION_LOGGER.error("", e);
        }
        catch (IOException e) {
            APPLICATION_LOGGER.error("", e);
        }
        if (code != 200) {
            APPLICATION_LOGGER.error("unable to access fancy log main page");
            return null;
        }
        else {
            return responseString;
        }
    }

    private HttpClient getAuthenticatedHttpClient(String strLogonURL, String strLogonUserId, String strLogonPassword) {
        HttpClient httpClient = new HttpClient();
        int code = 0;
        if (strLogonURL != null && strLogonUserId != null && strLogonPassword != null) {
            PostMethod postMethod = new PostMethod(strLogonURL);
            postMethod.setParameter("username", strLogonUserId);
            postMethod.setParameter("password", strLogonPassword);
            postMethod.setParameter("login-form-type", "pwd");
            try {
                code = httpClient.executeMethod(postMethod);
                System.out.println("Login Http Status " + code);
            }
            catch (HttpException e) {
                APPLICATION_LOGGER.error("", e);
            }
            catch (IOException e) {
                APPLICATION_LOGGER.error("", e);
            }
        }
        else {
            System.err.println("invalid logon configurations found");
        }
        if (code != 200) {
            System.out.println("Unable to login to server, Http Status Code = " + code);
            httpClient = null;
        }
        return httpClient;
    }

}
