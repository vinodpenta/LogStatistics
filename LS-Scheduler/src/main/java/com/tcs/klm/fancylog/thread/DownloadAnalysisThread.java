package com.tcs.klm.fancylog.thread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
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
import com.tcs.klm.fancylog.domain.Offer;
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
    private String downloadLocation;
    private String noOfDays;

    private static Map<String, StringBuffer> lstTempLogs = new HashMap<String, StringBuffer>();
    private static Map<String, List<LogKey>> lstTmpKeys = new HashMap<String, List<LogKey>>();
    private String COLLECTION_TRANSACTION;
    private String COLLECTION_LOGS;

    private Map<Offer, Integer> offerMap;

    public DownloadAnalysisThread(String logInURL, String userName, String passWord, String hyperLink, String sessionIDPossition, String downloadLocation, Map<String, LogAnalyzer> logAnalyzerMap,
                    MongoTemplate mongoTemplate, String noOfDays) {
        this.httpClient = FancySharedInfo.getInstance().getAuthenticatedHttpClient(logInURL, userName, passWord);
        this.hyperLink = hyperLink;
        this.sessionIDPossition = sessionIDPossition;
        this.logAnalyzerMap = logAnalyzerMap;
        this.mongoTemplate = mongoTemplate;
        this.downloadLocation = downloadLocation;
        COLLECTION_TRANSACTION = "transactions";// + "_" + FancySharedInfo.getInstance().getDay(FancySharedInfo.getInstance().getCalendar());
        COLLECTION_LOGS = "logs";// + "_" + FancySharedInfo.getInstance().getDay(FancySharedInfo.getInstance().getCalendar());
        this.noOfDays = noOfDays;
        offerMap = new HashMap<Offer, Integer>();
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
                APPLICATION_LOGGER.info("proccessing {}", fileName);
                BufferedInputStream isTextOrTail = new BufferedInputStream(getMethodLog.getResponseBodyAsStream());
                downloadFileContent(isTextOrTail, fileName);
                analizeFileContent(fileName);
                APPLICATION_LOGGER.info("done with {}", fileName);
            }
            else {
                APPLICATION_LOGGER.error("failed to download log file {}", hyperLink);
                APPLICATION_LOGGER.error("Http Status Code : {}", code);
            }
        }
        catch (Exception e) {
            APPLICATION_LOGGER.error("" + e);
        }

    }

    private void analizeFileContent(String fileName) {
        String year = Calendar.getInstance().get(Calendar.YEAR) + "";
        BufferedReader br = null;
        try {
            File file = new File(fileName);
            br = new BufferedReader(new FileReader(file));
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

            DBCollection offerCollection = mongoTemplate.getCollection("offer");
            Set<Offer> offerSet = offerMap.keySet();
            for (Offer offer : offerSet) {
                // increase no of offers by 1
                BasicDBObject newDocument = new BasicDBObject().append("$inc", new BasicDBObject().append("count", offerMap.get(offer).intValue()));
                // find query for productType and name
                BasicDBObject searchQuery = new BasicDBObject();
                searchQuery.append("productName", offer.getProductName()).append("productType", offer.getProductType()).append("productClass", offer.getProductClass()).append("date", offer.getDate());

                offerCollection.update(searchQuery, newDocument, true, false);
                /*
                 * System.out.println("productName :" + offer.getProductName() + "productType :" + offer.getProductType() + "productClass :" + offer.getProductClass() + "date :" + offer.getDate() + "count : " +
                 * offerMap.get(offer).intValue());
                 */
            }

            br.close();
            file.delete();
        }
        catch (Exception exception) {
            APPLICATION_LOGGER.error("excetion occured while Analyzing {}", exception);
            try {
                br.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private void downloadFileContent(BufferedInputStream isTextOrTail, String fileName) {
        APPLICATION_LOGGER.info("Downloading file {}", fileName);
        GZIPInputStream gzis = null;
        OutputStream out = null;
        try {
            File targetFile = new File(fileName);
            gzis = new GZIPInputStream(isTextOrTail);
            out = new FileOutputStream(targetFile);
            IOUtils.copy(gzis, out);
            APPLICATION_LOGGER.info("Downloading is finished file {}", fileName);
        }
        catch (Exception ex) {
            APPLICATION_LOGGER.error("Exception in downloadAnalysisThread {}", ex);
            FancySharedInfo.getInstance().setFaildHyperLinks(hyperLink);
        }
        finally {
            if (isTextOrTail != null) {
                try {
                    isTextOrTail.close();
                    gzis.close();

                }
                catch (Exception e) {
                    APPLICATION_LOGGER.error("" + e);
                }
            }
        }

    }

    private void processLastLine(String lineText, String sessionIDPossition, String year, String fileName) throws IOException {
        if (lineText.startsWith(year) && lineText.endsWith("Envelope>")) {
            String sessionID = null;
            String serviceName = null;
            String date = null;
            if (lineText.contains(".PROVIDER_REQUEST")) {
                sessionID = FancySharedInfo.getInstance().getSessionID(lineText, sessionIDPossition);
                serviceName = FancySharedInfo.getInstance().getServiceName(lineText);
                date = FancySharedInfo.getInstance().getDate(lineText);
                LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                if (logAnalyzer != null) {
                    List<LogKey> logKeys = logAnalyzer.getLogKeyFromRequest(lineText, mongoTemplate);
                    if (logKeys != null && !logKeys.isEmpty()) {
                        StringBuffer sbfTemp = new StringBuffer();
                        sbfTemp.append(fileName).append("\n");
                        sbfTemp.append(lineText).append("\n");
                        for (LogKey logKey : logKeys) {
                            logKey.setSessionID(sessionID);
                            logKey.setDate(date);
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, Integer.valueOf(noOfDays));
                            Date expireDate = cal.getTime();
                            logKey.setExpireAt(expireDate);
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
                    serviceName = FancySharedInfo.getInstance().getServiceName(lineText);
                    LogAnalyzer logAnalyzer = logAnalyzerMap.get(serviceName);
                    if (logAnalyzer != null) {
                        LogKey responseLogKey = logAnalyzer.getLogKeyFromResponse(lineText, mongoTemplate, offerMap);
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
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, Integer.valueOf(noOfDays));
                    Date expireDate = cal.getTime();
                    dBObjectLog.put("expireAt", expireDate);
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
