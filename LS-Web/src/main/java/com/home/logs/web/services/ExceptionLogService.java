package com.home.logs.web.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.home.logs.web.domain.LogKey;

@Component
public class ExceptionLogService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String COLLECTION_EXCEPTIONBEAN = "exceptionBean";
    private String COLLECTION_EXCEPTION = "exception";
    private String COLLECTION_TRANSACTION = "transactions";

    public List<LogKey> list(String id) {
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_EXCEPTIONBEAN);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));
        DBCursor cursor = dbCollection.find(searchQuery);
        List<LogKey> logKeys = new ArrayList<LogKey>();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            String className = (String) object.get("className");
            String exception = null;
            Object value = object.get("exception");
            if (value != null) {
                exception = value.toString();
            }
            String exceptionDate = (String) object.get("date");
            DBCollection dbCollectionException = mongoTemplate.getCollection(COLLECTION_EXCEPTION);
            searchQuery = new BasicDBObject();
            searchQuery.put("className", className);
            searchQuery.put("exception", exception);
            searchQuery.put("date", exceptionDate);
            BasicDBObject sortOrder = new BasicDBObject();
            sortOrder.put("date", 1);
            DBCursor cursorException = dbCollectionException.find(searchQuery).sort(sortOrder);
            while (cursorException.hasNext()) {
                DBObject exceptionObject = cursorException.next();
                String sessionId = (String) exceptionObject.get("sessionID");
                DBCollection dbCollectionTransaction = mongoTemplate.getCollection(COLLECTION_TRANSACTION);
                searchQuery = new BasicDBObject();
                searchQuery.put("sessionID", sessionId);
                DBCursor dbCursorTransactions = dbCollectionTransaction.find(searchQuery).sort(sortOrder);
                while (dbCursorTransactions.hasNext()) {
                    DBObject transaction = dbCursorTransactions.next();
                    LogKey logKey = new LogKey();
                    logKey.setChannel((String) transaction.get("channel"));
                    logKey.setPNR((String) transaction.get("PNR"));
                    logKey.setServiceName((String) transaction.get("serviceName"));
                    logKey.setHost((String) transaction.get("host"));
                    logKey.setMarket((String) transaction.get("market"));
                    value = transaction.get("sessionID");
                    if (value != null) {
                        logKey.setSessionID(value.toString());
                    }
                    value = transaction.get("date");
                    if (value != null) {
                        logKey.setDate(value.toString());
                    }
                    value = transaction.get("errorCode");
                    if (value != null) {
                        logKey.setErrorCode(value.toString());
                    }
                    value = transaction.get("errorDescription");
                    if (value != null) {
                        logKey.setErrorDescription(value.toString());
                    }
                    logKeys.add(logKey);
                }
                // searchQuery.put("", val)
            }

        }

        System.out.println(logKeys.get(0).getPNR() + logKeys.get(0).getErrorCode());
        return logKeys;
    }

}
