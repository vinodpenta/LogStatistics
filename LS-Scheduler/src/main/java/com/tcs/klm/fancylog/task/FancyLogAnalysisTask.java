package com.tcs.klm.fancylog.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tcs.klm.fancylog.analysis.LogAnalyzer;
import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.FancySharedInfo;
import com.tcs.klm.fancylog.utils.Utils;

//import org.apache.commons.lang.stringutils;

@Component(value = "fancyLogAnalysisTask")
public class FancyLogAnalysisTask {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Map<String, LogAnalyzer> logAnalyzerMap;

    private static final String COLLECTION_SETTINGS = "settings";
    private String COLLECTION_TRANSACTION;
    private String COLLECTION_LOGS;

    private static Map<String, StringBuffer> lstTempLogs = new HashMap<String, StringBuffer>();
    private static Map<String, List<LogKey>> lstTmpKeys = new HashMap<String, List<LogKey>>();

    // private static Map<String, String> lstTmpsessionIdUPR = new HashMap<String, String>();
    // private static Map<String, String> lstTmpsessionMap = new HashMap<String, String>();
    // private static Map<String, List<String>> map = new HashMap<String, List<String>>();

    public void performTask() throws IOException {
        System.out.println("FancyLogAnalysisTask" + System.currentTimeMillis());
        FancySharedInfo.getInstance().setAnalysisInProgress(true);
        DBCollection settingsCollection = mongoTemplate.getCollection(COLLECTION_SETTINGS);
        DBCursor settingsCursor = settingsCollection.find();
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        while (settingsCursor.hasNext()) {
            DBObject settings = settingsCursor.next();
            // String applicationName = (String) settings.get("applicationName");
            COLLECTION_TRANSACTION = "transactions";
            COLLECTION_LOGS = "logs";
            if (!mongoTemplate.collectionExists(COLLECTION_LOGS)) {
                mongoTemplate.createCollection(COLLECTION_LOGS);
            }
            if (!mongoTemplate.collectionExists(COLLECTION_TRANSACTION)) {
                mongoTemplate.createCollection(COLLECTION_TRANSACTION);
            }
            String sessionIDPossition = (String) settings.get("sessionIdPosition");
            String gzFileLocation = (String) settings.get("downloadLocation");
            String[] names = StringUtils.split(gzFileLocation, "/");
            String tempFileLocation = gzFileLocation.replace(names[names.length - 1], "temp");
            File[] files = getListOfFiles(gzFileLocation);
            if (files != null) {
                (new File(tempFileLocation)).mkdirs();
                StringBuffer sbf = null;
                String sCurrentLine = null;
                for (File file : files) {
                    File tmpFile = getUnZipedFile(file, tempFileLocation);
                    if (tmpFile != null) {
                        BufferedReader br = new BufferedReader(new FileReader(tmpFile));
                        sbf = new StringBuffer();
                        while ((sCurrentLine = br.readLine()) != null) {
                            if (sCurrentLine.startsWith(year)) {
                                try {
                                    processLastLine(sbf.toString(), sessionIDPossition, year, file.getName());
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
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
                    tmpFile.delete();
                }
                File gzfolder = new File(gzFileLocation);
                deleteDirectory(gzfolder);
            }

        }
        System.out.println("FancyLogAnalysisTask end" + System.currentTimeMillis());
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
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
            else if (lineText.contains(".CONSUMER_RE")) {
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
        if (line.startsWith("2014-")) {
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

    private File getUnZipedFile(File file, String tempFileLocation) {
        File tempfile = null;
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
            String fileName = file.getName();
            fileName = fileName.replace("gz", "log");
            System.out.println(tempFileLocation);
            String unzipfilepath = tempFileLocation + fileName;
            System.out.println(unzipfilepath);
            FileOutputStream out = new FileOutputStream(unzipfilepath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            out.close();
            System.out.println(unzipfilepath);
            tempfile = new File(unzipfilepath);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return tempfile;
    }

    private static File[] getListOfFiles(String directoryPath) {
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }

    // private String getCollectionName() {
    // Calendar calendar = Calendar.getInstance();
    // String collectionName = calendar.get(Calendar.YEAR) + StringUtils.leftPad("" + (calendar.get(Calendar.MONTH) + 1), 2, "0") + StringUtils.leftPad("" + calendar.get(Calendar.DATE), 2, "0");
    // return collectionName;
    // }
}
