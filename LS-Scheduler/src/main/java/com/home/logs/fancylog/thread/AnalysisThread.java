package com.home.logs.fancylog.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.home.logs.fancylog.analysis.LogAnalyzer;
import com.home.logs.fancylog.domain.LogKey;
import com.home.logs.fancylog.utils.FancySharedInfo;
import com.home.logs.fancylog.utils.Utils;

@Component
@Scope("prototype")
public class AnalysisThread implements Runnable {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(AnalysisThread.class);

    private MongoTemplate mongoTemplate;

    private Map<String, LogAnalyzer> logAnalyzerMap;

    private File file;
    private String sessionIDPossition;

    private static Map<String, StringBuffer> lstTempLogs = new HashMap<String, StringBuffer>();
    private static Map<String, List<LogKey>> lstTmpKeys = new HashMap<String, List<LogKey>>();

    private String COLLECTION_TRANSACTION = "transactions";
    private String COLLECTION_LOGS = "logs";

    public AnalysisThread(File file, String sessionIDPossition, Map<String, LogAnalyzer> logAnalyzerMap, MongoTemplate mongoTemplate) {
        this.file = file;
        this.sessionIDPossition = sessionIDPossition;
        this.logAnalyzerMap = logAnalyzerMap;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run() {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = calendar.get(Calendar.YEAR) + "";
            if (file != null) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuffer sbf = new StringBuffer();
                String sCurrentLine = null;
                while ((sCurrentLine = br.readLine()) != null) {
                    if (sCurrentLine.startsWith(year)) {
                        try {
                            processLastLine(sbf.toString(), sessionIDPossition, year, file.getName());
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
                br.close();
            }
            file.delete();
        }
        catch (Exception e) {
            APPLICATION_LOGGER.error("exception occured while analyzing {}", e);
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
                    List<LogKey> logKeys = logAnalyzer.getLogKeyFromRequest(xmlPayload, null);
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
                        LogKey responseLogKey = logAnalyzer.getLogKeyFromResponse(xmlPayload, null, null);
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
