package com.tcs.klm.fancylog.task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import org.springframework.stereotype.Component;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tcs.klm.fancylog.thread.DownloadThread;
import com.tcs.klm.fancylog.utils.FancySharedInfo;

@Component(value = "fancyLogDownloadTask")
public class FancyLogDownloadTask {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(FancyLogDownloadTask.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String COLLECTION_NAME = "settings";

    public void performTask() {
        FancySharedInfo.getInstance().setDownloadInProgress(true);
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_NAME);
        DBCursor dbCursor = dbCollection.find();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            String applicationName = (String) object.get("applicationName");
            String fancyLogURLPattern = (String) object.get("fancyLogURLPattern");
            String host = (String) object.get("host");
            String nodeList = (String) object.get("nodeList");
            String[] nodes = nodeList.split(",");
            String instance = (String) object.get("instance");
            String logInURL = (String) object.get("logInURL");
            String userName = (String) object.get("userName");
            String passWord = (String) object.get("passWord");
            String fileName = (String) object.get("fileName");
            String fileNames[] = StringUtils.split(fileName, ",");
            String downloadLocation = (String) object.get("downloadLocation");
            List<String> lstHyeperLink = new ArrayList<String>();

            HttpClient httpClient = getAuthenticatedHttpClient(logInURL, userName, passWord);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -3);
            Date today = calendar.getTime();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH");
            String date = formatter.format(today);

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
                starFileDownload(logInURL, userName, passWord, lstHyeperLink, downloadLocation);
                FancySharedInfo.getInstance().setDownloadInProgress(false);
                FancySharedInfo.getInstance().setLastTaskSuccessful(true);

            }
        }
    }

    private boolean starFileDownload(String logInURL, String userName, String passWord, List<String> listHyeperLink, String downloadLocation) {
        boolean downloadSuccessFlag = false;
        if (listHyeperLink != null && !listHyeperLink.isEmpty()) {
            try {
                Runnable task;
                APPLICATION_LOGGER.info("Download Started... {}", System.currentTimeMillis());
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
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        APPLICATION_LOGGER.info("Download Completed...  >> {} ", System.currentTimeMillis());
        return downloadSuccessFlag;
    }

    private boolean isValid(String logFileURL, String[] fileNames, String date) {
        boolean flag = false;
        flag = (logFileURL.contains(".gz") || logFileURL.contains(".zip")) && logFileURL.contains("action=redir") && logFileURL.contains("oldlogs") && logFileURL.contains(date);
        if (flag && fileNames != null) {
            for (String fileName : fileNames)
                flag = logFileURL.contains(fileName);
        }
        else {
            flag = false;
        }
        return flag;
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
                APPLICATION_LOGGER.info("Login Http Status {} ", code);
            }
            catch (HttpException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            APPLICATION_LOGGER.error("invalid logon configurations found");
        }
        if (code != 200) {
            APPLICATION_LOGGER.error("Unable to login to server, Http Status Code = {}", code);
            httpClient = null;
        }
        return httpClient;
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
