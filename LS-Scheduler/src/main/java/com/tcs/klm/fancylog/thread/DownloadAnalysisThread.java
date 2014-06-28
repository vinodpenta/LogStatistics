package com.tcs.klm.fancylog.thread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tcs.klm.fancylog.analysis.LogAnalyzer;
import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.FancySharedInfo;
import com.tcs.klm.fancylog.utils.Utils;

@Component
@Scope("prototype")
public class DownloadAnalysisThread implements Runnable {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(DownloadAnalysisThread.class);

    private HttpClient httpClient;
    private String hyperLink;
    private String sessionIDPossition;
    private Map<String, LogAnalyzer> logAnalyzerMap;
    private MongoTemplate mongoTemplate;

    private static Map<String, StringBuffer> lstTempLogs = new HashMap<String, StringBuffer>();
    private static Map<String, List<LogKey>> lstTmpKeys = new HashMap<String, List<LogKey>>();
    private String COLLECTION_TRANSACTION = "transactions";
    private String COLLECTION_LOGS = "logs";

    public DownloadAnalysisThread(String logInURL, String userName, String passWord, String hyperLink, String sessionIDPossition, Map<String, LogAnalyzer> logAnalyzerMap, MongoTemplate mongoTemplate) {
        this.httpClient = FancySharedInfo.getInstance().getAuthenticatedHttpClient(logInURL, userName, passWord);
        this.hyperLink = hyperLink;
        this.sessionIDPossition = sessionIDPossition;
        this.logAnalyzerMap = logAnalyzerMap;
        this.mongoTemplate = mongoTemplate;
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
                String fileName = hyperLink.substring(fileNameBeginIndex, fileNameEndIndex);
                fileName = fileName.replace(".gz", ".log");
                APPLICATION_LOGGER.info("proccessing {}",fileName);
                BufferedInputStream isTextOrTail = new BufferedInputStream(getMethodLog.getResponseBodyAsStream());
                analyzeFileContent(isTextOrTail, fileName);
                APPLICATION_LOGGER.info("done with {}",fileName);
                isTextOrTail.close();
                // downloadSuccessFlag = true;
            }
            else {
                APPLICATION_LOGGER.error("failed to download log file {}", hyperLink);
                APPLICATION_LOGGER.error("Http Status Code : {}", code);
            }
        }
        catch (HttpException e) {
            APPLICATION_LOGGER.error(""+e);
        }
        catch (IOException e) {
        	APPLICATION_LOGGER.error(""+e);
        }

    }

    private void analyzeFileContent(BufferedInputStream isTextOrTail, String fileName) {
        APPLICATION_LOGGER.info("Downloading and Analysing file {}", fileName);
        BufferedReader br = null;
        GZIPInputStream gzis = null;
        try {
            String year = Calendar.getInstance().get(Calendar.YEAR) + "";
            gzis = new GZIPInputStream(isTextOrTail);
            br = new BufferedReader(new InputStreamReader(gzis));
            StringBuffer sbf = new StringBuffer();
            String sCurrentLine = null;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith(year)) {
                    try {
                        processLastLine(sbf.toString(), sessionIDPossition, year, fileName);
                    }
                    catch (Exception e) {
                        APPLICATION_LOGGER.error(sbf.toString());
                        APPLICATION_LOGGER.error(e.getMessage());
                    }
                    sbf.delete(0, sbf.length());
                    sbf.append(sCurrentLine);
                }
                else {
                    sbf.append(sCurrentLine);
                }
            }
        }
        catch (Exception ex) {
            APPLICATION_LOGGER.error("Exception in ListAvailableProductsController {}", ex);
        }
        finally {
            if (isTextOrTail != null) {
                try {
                    isTextOrTail.close();
                    br.close();
                    gzis.close();

                }
                catch (Exception e) {
                	APPLICATION_LOGGER.error(""+e);
                }
            }
        }

    }

    private void processLastLine(String lineText, String sessionIDPossition, String year, String fileName) throws IOException {
        if (lineText.startsWith(year) && lineText.endsWith("Envelope>")) {
            String xmlPayload = lineText.substring(lineText.indexOf("<?xml version="));
            String sessionID = null;
            String serviceName = null;
            String date = null;
            if (lineText.contains(".PROVIDER_REQUEST")) {
                sessionID = FancySharedInfo.getInstance().getSessionID(lineText, sessionIDPossition);
                serviceName = FancySharedInfo.getInstance().getServiceName(xmlPayload);
                date = FancySharedInfo.getInstance().getDate(lineText);
                LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                if (logAnalyzer != null) {
                    List<LogKey> logKeys = logAnalyzer.getLogKeyFromRequest(xmlPayload);
                    if (logKeys != null && !logKeys.isEmpty()) {
                        StringBuffer sbfTemp = new StringBuffer();
                        sbfTemp.append(fileName).append("\n");
                        sbfTemp.append(lineText).append("\n");
                        for (LogKey logKey : logKeys) {
                            logKey.setSessionID(sessionID);
                            logKey.setDate(date);
                        }
                        lstTempLogs.put(sessionID, sbfTemp);
                        lstTmpKeys.put(sessionID, logKeys);
                    }
                }
            }
            else if (lineText.contains(".PROVIDER_RESPONSE")) {
                sessionID = FancySharedInfo.getInstance().getSessionID(lineText, sessionIDPossition);
                if (lstTmpKeys.containsKey(sessionID)) {
                    lstTempLogs.get(sessionID).append(lineText).append("\n");
                    serviceName = FancySharedInfo.getInstance().getServiceName(xmlPayload);
                    LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                    if (logAnalyzer != null) {
                        LogKey responseLogKey = logAnalyzer.getLogKeyFromResponse(xmlPayload);
                        if (responseLogKey != null) {
                            List<LogKey> logKeys = lstTmpKeys.get(sessionID);
                            for (LogKey logKey : logKeys) {
                                logKey.setErrorCode(responseLogKey.getErrorCode());
                                logKey.setErrorDescription(responseLogKey.getErrorDescription());
                            }
                        }
                    }
                    StringBuffer log = lstTempLogs.get(sessionID);
                    String strLog = log.toString();
                    String compressedLog = Utils.compress(strLog);

                    DBCollection dbCollectionLog = mongoTemplate.getCollection(COLLECTION_LOGS);

                    DBObject dBObjectLog = new BasicDBObject();
                    dBObjectLog.put("log", compressedLog);
                    Date today = Calendar.getInstance().getTime();
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH");
                    String date1 = formatter.format(today);
                    dBObjectLog.put("date", date1);
                    dbCollectionLog.insert(dBObjectLog);
                    String logID = dBObjectLog.get("_id").toString();

                    List<LogKey> logKeys = lstTmpKeys.get(sessionID);
                    for (LogKey key : logKeys) {
                        key.setLogID(logID);
                        mongoTemplate.insert(key, COLLECTION_TRANSACTION);
                    }

                    lstTmpKeys.remove(sessionID);
                    lstTempLogs.remove(sessionID);

                }
                else {
                    lstTmpKeys.remove(sessionID);
                    lstTempLogs.remove(sessionID);
                }
            }
            else {
                sessionID = FancySharedInfo.getInstance().getSessionID(lineText, sessionIDPossition);
                if (lstTmpKeys.containsKey(sessionID)) {
                    lstTempLogs.get(sessionID).append(lineText).append("\n");
                }
            }
        }
    }

}
