package com.tcs.klm.fancylog.thread;

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
import com.tcs.klm.fancylog.analysis.LogAnalyzer;
import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.Utils;

@Component
@Scope("prototype")
public class AnalysisThread implements Runnable {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(AnalysisThread.class);

    private MongoTemplate mongoTemplate;

    private Map<String, LogAnalyzer> logAnalyzerMap;

    private File file;
    private String tempFileLocation;
    private String sessionIDPossition;

    private static Map<String, StringBuffer> lstTempLogs = new HashMap<String, StringBuffer>();
    private static Map<String, List<LogKey>> lstTmpKeys = new HashMap<String, List<LogKey>>();

    private String COLLECTION_TRANSACTION = "transactions";
    private String COLLECTION_LOGS = "logs";

    public AnalysisThread(File file, String tempFileLocation, String sessionIDPossition, Map<String, LogAnalyzer> logAnalyzerMap, MongoTemplate mongoTemplate) {
        this.file = file;
        this.tempFileLocation = tempFileLocation;
        this.sessionIDPossition = sessionIDPossition;
        this.logAnalyzerMap = logAnalyzerMap;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run() {
        try {
            // File tmpFile = getUnZipedFile(file, tempFileLocation);
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
            e.printStackTrace();
        }
    }

    private void processLastLine(String lineText, String sessionIDPossition, String year, String fileName) throws IOException {
        if (lineText.startsWith(year) && lineText.endsWith("Envelope>")) {
            String xmlPayload = lineText.substring(lineText.indexOf("<?xml version="));
            String sessionID = null;
            String serviceName = null;
            String date = null;
            if (lineText.contains(".PROVIDER_REQUEST")) {
                sessionID = getSessionID(lineText, sessionIDPossition);
                serviceName = getServiceName(xmlPayload);
                date = getDate(lineText);
                LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                if (logAnalyzer != null) {
                    List<LogKey> logKeys = logAnalyzer.getLogKeyFromRequest(xmlPayload);
                    if (logKeys != null && !logKeys.isEmpty()) {
                        StringBuffer sbfTemp = new StringBuffer();
                        sbfTemp.append(fileName).append("\n");
                        sbfTemp.append(lineText).append("\n");
                        for (LogKey logKey : logKeys) {
                            // lstTmpsessionIdUPR.put(logKey.getPassengerId(), sessionID);
                            logKey.setSessionID(sessionID);
                            logKey.setDate(date);
                        }
                        lstTempLogs.put(sessionID, sbfTemp);
                        lstTmpKeys.put(sessionID, logKeys);
                    }
                }
            }
            else if (lineText.contains(".PROVIDER_RESPONSE")) {
                sessionID = getSessionID(lineText, sessionIDPossition);
                if (lstTmpKeys.containsKey(sessionID)) {
                    lstTempLogs.get(sessionID).append(lineText).append("\n");
                    serviceName = getServiceName(xmlPayload);
                    LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                    if (logAnalyzer != null) {
                        LogKey responseLogKey = logAnalyzer.getLogKeyFromResponse(xmlPayload);
                        if (responseLogKey != null) {
                            List<LogKey> logKeys = lstTmpKeys.get(sessionID);
                            for (LogKey logKey : logKeys) {
                                logKey.setErrorCode(responseLogKey.getErrorCode());
                                logKey.setErrorDescription(responseLogKey.getErrorDescription());
                                // lstTmpsessionIdUPR.remove(logKey.getPassengerId());
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
                    /*
                     * List<String> wmSessionIDs = map.get(sessionID); if (wmSessionIDs != null) { for (String wmSessionID : wmSessionIDs) { //lstTmpsessionMap.remove(wmSessionID); } }
                     */
                    // map.remove(sessionID);
                }
                else {
                    lstTmpKeys.remove(sessionID);
                    lstTempLogs.remove(sessionID);
                }
            }
            else {
                sessionID = getSessionID(lineText, sessionIDPossition);
                // Set<String> keySet = lstTmpsessionMap.keySet();
                if (lstTmpKeys.containsKey(sessionID)) {
                    lstTempLogs.get(sessionID).append(lineText).append("\n");
                    /*
                     * if (lineText.contains("GetEticketDetails.CONSUMER_RESPONSE")) { serviceName = getServiceName(xmlPayload); LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName); LogKey responseLogKey =
                     * logAnalyzer.getLogKeyFromResponse(xmlPayload); if (responseLogKey != null) { List<LogKey> logKeys = lstTmpKeys.get(sessionID); for (LogKey logKey : logKeys) {
                     * logKey.setPNR(responseLogKey.getPNR()); } } }
                     */
                }
                /*
                 * else if (keySet.contains(sessionID)) { String flowSessionId = lstTmpsessionMap.get(sessionID); if (lstTempLogs.get(flowSessionId) != null) lstTempLogs.get(flowSessionId).append(lineText).append("\n");
                 * }
                 */
                /*
                 * else { String passengerId = getPassengerId(lineText); if (passengerId != null) { String flowSessionId = lstTmpsessionIdUPR.get(passengerId); if (lstTempLogs.get(flowSessionId) != null) {
                 * lstTempLogs.get(flowSessionId).append(lineText).append("\n"); lstTmpsessionMap.put(sessionID, flowSessionId); if (map.containsKey(flowSessionId)) { map.get(flowSessionId).add(sessionID); } else {
                 * List<String> wmSessionIDs = new ArrayList<String>(); wmSessionIDs.add(sessionID); map.put(flowSessionId, wmSessionIDs); } } } }
                 */

            }
        }
    }

    private String getDate(String line) {
        String dateString = null;
        Calendar calendar = Calendar.getInstance();
        if (line.startsWith(calendar.get(Calendar.YEAR) + "")) {
            String strs[] = line.split(" ");
            dateString = strs[0] + " " + strs[1];// line.substring(24, 47);
        }
        /*
         * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS"); Date date = null; try { date = sdf.parse(dateString); } catch (ParseException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        return dateString;
    }

    // private String getPassengerId(String lineText) {
    // String passengerId = null;
    // try {
    // passengerId = lineText.substring(lineText.indexOf("<ns3:uniquePassengerReference>") + "<ns3:uniquePassengerReference>".length(), lineText.indexOf("</ns3:uniquePassengerReference>"));
    // }
    // catch (IndexOutOfBoundsException e) {
    // return null;
    // }
    // return passengerId;
    // }

    private String getServiceName(String xmlPayload) {
        String serviceName = null;
        int bodyIndex = xmlPayload.indexOf("Body>") + 5;
        String serviceNamePartOne = xmlPayload.substring(bodyIndex, xmlPayload.indexOf(" ", bodyIndex));
        String serviceNamePartTwo = null;
        if (!serviceNamePartOne.contains(">")) {
            serviceNamePartTwo = serviceNamePartOne.substring(serviceNamePartOne.indexOf(":") + 1);
            serviceName = serviceNamePartTwo;
        }
        else {
            serviceName = serviceNamePartOne;
        }
        if (serviceName != null && serviceName.contains("<")) {
            serviceName = serviceName.substring(serviceName.indexOf("<") + 1);
        }
        if (serviceName != null && serviceName.contains("Request")) {
            serviceName = serviceName.substring(0, serviceName.indexOf("Request"));
        }
        else if (serviceName != null && serviceName.contains("Response")) {
            serviceName = serviceName.substring(0, serviceName.indexOf("Response"));
        }
        else if (serviceName != null && serviceName.contains("_IN")) {
            serviceName = serviceName.substring(0, serviceName.indexOf("_IN"));
        }
        else if (serviceName != null && serviceName.contains("_OUT")) {
            serviceName = serviceName.substring(0, serviceName.indexOf("_OUT"));
        }
        return serviceName;
    }

    private String getSessionID(String lineText, String sessionIDPossition) {
        String sessionID = new String();
        String strs[] = lineText.split(" ");
        sessionID = strs[Integer.valueOf(sessionIDPossition) - 1];
        return sessionID;
    }

}
