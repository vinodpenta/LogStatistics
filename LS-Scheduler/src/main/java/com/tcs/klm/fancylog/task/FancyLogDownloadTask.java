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

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String COLLECTION_NAME = "settings";

    public void performTask() {
        // System.out.println("FancyLogDownloadTask");
        FancySharedInfo.getInstance().setDownloadInProgress(true);
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_NAME);
        // System.out.println(dbCollection.getName());
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
                            if (isValid(logFileURL, fileName, date)) {
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
                System.out.println("Download Started..." + System.currentTimeMillis());
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
                // check active thread, if zero then shut down the thread pool
                /*
                 * for (;;) { int count = taskExecutor.getActiveCount(); System.out.println("Active Threads : " + count); try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); } if (count ==
                 * 0) { taskExecutor.shutdown(); break; } }
                 */
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("Download Completed...  >> " + System.currentTimeMillis());
        return downloadSuccessFlag;
    }

    private boolean isValid(String logFileURL, String fileName, String date) {
        boolean flag = false;
        flag = (logFileURL.contains(".gz") || logFileURL.contains(".zip")) && logFileURL.contains("action=redir") && logFileURL.contains("oldlogs") && logFileURL.contains(fileName) && logFileURL.contains(date);
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
                System.out.println("Login Http Status " + code);
            }
            catch (HttpException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
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

    private String getFancyLogMainPage(HttpClient httpClient, String strFancyLogMainURL) {
        String responseString = null;
        GetMethod getMethod = new GetMethod(strFancyLogMainURL);
        // System.out.println("Processing Fancy Log Main Page URL " + strFancyLogMainURL);
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
            System.out.println("unable to access fancy log main page");
            return null;
        }
        else {
            return responseString;
        }
    }
}
