package com.tcs.klm.web.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tcs.klm.fancylog.domain.LogKey;

@Component
public class SearchLogsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String COLLECTION_TRANSACTION = "transactions";
    private String COLLECTION_LOGS = "logs";

    public List<LogKey> searchResults(String pnr) {
        // Query query = new Query();
        // query.addCriteria(Criteria.where("PNR").is(pnr));
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("PNR", pnr);
        DBCollection collection = mongoTemplate.getCollection(COLLECTION_TRANSACTION);
        
        BasicDBObject sortOrder = new BasicDBObject();
        sortOrder.put("date", 1); // order DESC 
        
        DBCursor cursor = collection.find(searchQuery).sort(sortOrder);
        List<LogKey> logKeys = new ArrayList<LogKey>();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            LogKey logKey = new LogKey();
            logKey.setChannel((String) object.get("channel"));
            logKey.setPNR((String) object.get("PNR"));
            logKey.setServiceName((String) object.get("serviceName"));
            logKey.setHost((String) object.get("host"));
            logKey.setMarket((String) object.get("market"));
            Object value = object.get("sessionID");
            if (value != null) {
                logKey.setSessionID(value.toString());
            }
            value = object.get("date");
            if (value != null) {
                logKey.setDate(value.toString());
            }
            value = object.get("errorcode");
            if (value != null) {
                logKey.setErrorCode(value.toString());
            }
            value = object.get("errorDescription");
            if (value != null) {
                logKey.setErrorDescription(value.toString());
            }
            logKeys.add(logKey);
        }
        return logKeys;
    }

    public String getLogs(String id) throws IOException {
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_TRANSACTION);
        BasicDBObject query = new BasicDBObject();
        query.put("sessionID", id);
        DBCursor cursor = dbCollection.find(query);
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            String compressedLogID = (String) object.get("logID");
            DBCollection dbCollectionlog = mongoTemplate.getCollection(COLLECTION_LOGS);
            BasicDBObject querylog = new BasicDBObject();
            querylog.put("_id", new ObjectId(compressedLogID));
            DBCursor cursorlog = dbCollectionlog.find(querylog);
            while (cursorlog.hasNext()) {
                DBObject objectlog = cursorlog.next();
                String compressedLog = (String) objectlog.get("log");
                String log = decompress(compressedLog);
                String modifyedLog = formatLog(log);
                return modifyedLog;
            }

        }
        return null;
    }

    private String formatLog(String log) {
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        String formattedLog = log.replaceAll("log.gz" + year + "-", "log.gz\n\n" + year + "-");
        formattedLog = formattedLog.replaceAll("Envelope>" + year + "-", "Envelope>\n" + year + "-");
        return formattedLog;
    }

    private String decompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        // System.out.println("Input String length : " + str.length());
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        // System.out.println("Output String lenght : " + outStr.length());
        return outStr;
    }
}
