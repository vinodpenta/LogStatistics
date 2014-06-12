package com.tcs.klm.web.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
        Query query = new Query();
        query.addCriteria(Criteria.where("pnr").is(pnr));
        List<LogKey> logKeys = mongoTemplate.find(query, LogKey.class);
        return logKeys;
    }

    public String getLogs(String id) throws IOException {
        DBCollection dbCollection = mongoTemplate.getCollection(COLLECTION_LOGS);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", id);
        DBCursor cursor = dbCollection.find(query);
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            String compressedLog = (String) object.get("log");
            String log = decompress(compressedLog);
            return log;
        }
        return null;
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
