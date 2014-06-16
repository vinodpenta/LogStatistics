package com.tcs.klm.fancylog.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DownloadThread implements Runnable {

    private HttpClient httpClient;
    private String hyperLink;
    private String downloadLocation;

    public DownloadThread(HttpClient httpClient, String hyperLink, String downloadLocation) {
        this.downloadLocation = downloadLocation;
        this.httpClient = httpClient;
        this.hyperLink = hyperLink;
    }

    @Override
    public void run() {

        GetMethod getMethodLog = new GetMethod(hyperLink);
        try {
            int code = httpClient.executeMethod(getMethodLog);
            if (code == 200) {
                System.out.println("response code 200");
                int fileNameBeginIndex = hyperLink.indexOf("oldlogs/") + "oldlogs/".length();
                int fileNameEndIndex = hyperLink.indexOf("&app=");
                System.out.println(hyperLink);
                (new File(downloadLocation)).mkdirs();
                String fileName = downloadLocation + hyperLink.substring(fileNameBeginIndex, fileNameEndIndex);
                fileName = fileName.replace(".gz", ".log.gz");
                InputStream isTextOrTail = getMethodLog.getResponseBodyAsStream();
                saveFileContent(isTextOrTail, fileName);
                // downloadSuccessFlag = true;
            }
            else {
                System.out.println("failed to download log file " + hyperLink);
                System.out.println("Http Status Code : " + code);
            }
        }
        catch (HttpException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveFileContent(InputStream isTextOrTail, String fileName) {
        OutputStream out = null;
        File targetFile = new File(fileName);
        System.out.println("Downloading file " + targetFile.getPath());
        try {

            out = new FileOutputStream(targetFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = isTextOrTail.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
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
