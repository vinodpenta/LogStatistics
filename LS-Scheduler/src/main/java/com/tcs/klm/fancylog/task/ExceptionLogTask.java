package com.tcs.klm.fancylog.task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ExceptionLogTask {

    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String COLLECTION_SETTINGS = "settings";

    public void perfoemTask() {
        System.out.println("ExceptionLogTask");
        DBCollection settingsCollection = mongoTemplate.getCollection(COLLECTION_SETTINGS);
        DBCursor settingsCursor = settingsCollection.find();
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
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
            String fileName = (String) settings.get("fileName");
            String downloadLocation = (String) settings.get("downloadLocation");
            List<String> lstHyeperLink = new ArrayList<String>();

            HttpClient httpClient = getAuthenticatedHttpClient(logInURL, userName, passWord);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar1.getTime();
            // needs to change date format
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH");
            String date = formatter.format(yesterday);
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

}
