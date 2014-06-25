package com.tcs.klm.web.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tcs.klm.web.domain.ExceptionBean;

@Component
public class ExceptionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String COLLECTION_EXCEPTION = "exception";

    public List<ExceptionBean> list(String date) {
        Map<String, ExceptionBean> exceptionBeanMap = new HashMap<String, ExceptionBean>();

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", date);
        DBCollection collection = mongoTemplate.getCollection(COLLECTION_EXCEPTION);
        DBCursor cursor = collection.find(searchQuery);
        while (cursor.hasNext()) {
            String key;
            DBObject object = cursor.next();
            String className = (String) object.get("className");
            String errorDescription = (String) object.get("errorDescription");
            String exception = null;
            Object value = object.get("exception");
            if (value != null) {
                exception = value.toString();
            }
            key = className + errorDescription + exception;
            ExceptionBean exceptionBean = exceptionBeanMap.get(key);
            if (exceptionBean != null) {
                exceptionBean.incrementCount();
            }
            else {
                ExceptionBean exceptionBean1 = new ExceptionBean();
                exceptionBean1.setClassName(className);
                exceptionBean1.setExceptionDescription(errorDescription);
                exceptionBeanMap.put(key, exceptionBean1);
            }
        }

        List<ExceptionBean> exceptionBeans = new ArrayList<ExceptionBean>();
        Set<String> keySet = exceptionBeanMap.keySet();
        if (keySet != null) {
            for (String key : keySet) {
                exceptionBeans.add(exceptionBeanMap.get(key));
            }
        }
        return exceptionBeans;
    }
}
