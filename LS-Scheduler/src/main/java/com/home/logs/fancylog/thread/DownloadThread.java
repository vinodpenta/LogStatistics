package com.home.logs.fancylog.thread;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DownloadThread implements Runnable {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(DownloadThread.class);

    private HttpClient httpClient;
    private String hyperLink;
    private String downloadLocation;

    public DownloadThread(HttpClient httpClient, String hyperLink, String downloadLocation) {
        this.downloadLocation = downloadLocation;
        this.httpClient = httpClient;
        this.hyperLink = hyperLink;
    }

    public DownloadThread(String logInURL, String userName, String passWord, String hyperLink, String downloadLocation) {
        this.downloadLocation = downloadLocation;
        this.httpClient = getAuthenticatedHttpClient(logInURL, userName, passWord);
        this.hyperLink = hyperLink;
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
                APPLICATION_LOGGER.info("Login Http Status {}", code);
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
            APPLICATION_LOGGER.error("Unable to login to server, Http Status Code = {} ", code);
            httpClient = null;
        }
        return httpClient;
    }

    @Override
    public void run() {

        GetMethod getMethodLog = new GetMethod(hyperLink);
        try {
            int code = httpClient.executeMethod(getMethodLog);
            if (code == 200) {
                APPLICATION_LOGGER.info("response code 200");
                int fileNameBeginIndex = hyperLink.indexOf("oldlogs/") + "oldlogs/".length();
                int fileNameEndIndex = hyperLink.indexOf("&app=");
                (new File(downloadLocation)).mkdirs();
                String fileName = downloadLocation + hyperLink.substring(fileNameBeginIndex, fileNameEndIndex);
                fileName = fileName.replace(".gz", ".log");
                BufferedInputStream isTextOrTail = new BufferedInputStream(getMethodLog.getResponseBodyAsStream());
                saveFileContent(isTextOrTail, fileName);
                isTextOrTail.close();
                // downloadSuccessFlag = true;
            }
            else {
                APPLICATION_LOGGER.error("failed to download log file {}", hyperLink);
                APPLICATION_LOGGER.error("Http Status Code : {}", code);
            }
        }
        catch (HttpException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveFileContent(BufferedInputStream isTextOrTail, String fileName) {
        OutputStream out = null;
        File targetFile = new File(fileName);

        APPLICATION_LOGGER.info("Downloading file {}", targetFile.getPath());
        try {
            GZIPInputStream gzis = new GZIPInputStream(isTextOrTail);
            out = new FileOutputStream(targetFile);
            byte[] buf = new byte[32 * 1024];
            int len;

            while ((len = gzis.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // IOUtils.copy(isTextOrTail, out);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (isTextOrTail != null) {
                try {
                    isTextOrTail.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

}
