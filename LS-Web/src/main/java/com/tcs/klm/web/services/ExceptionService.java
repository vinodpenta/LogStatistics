package com.tcs.klm.web.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String COLLECTION_EXCEPTIONBEAN = "exceptionBean";

    public List<ExceptionBean> list(String date) {
        Map<String, ExceptionBean> exceptionBeanMap = new HashMap<String, ExceptionBean>();
        BasicDBObject searchQuery = new BasicDBObject();

        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Date selectedDate = null;
        String selectedDateString = null;
        try {
            selectedDate = formatter.parse(date);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            selectedDateString = df.format(selectedDate);

        }
        catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<ExceptionBean> exceptionBeans = new ArrayList<ExceptionBean>();

        BasicDBObject sortOrder = new BasicDBObject();
        sortOrder.put("count", -1);

        searchQuery.put("date", selectedDateString);
        DBCollection collection = mongoTemplate.getCollection(COLLECTION_EXCEPTIONBEAN);
        DBCursor cursor = collection.find(searchQuery).sort(sortOrder);
        while (cursor.hasNext()) {
            String key;
            DBObject object = cursor.next();

            ExceptionBean exceptionBean = new ExceptionBean();

            String className = (String) object.get("className");
            String exception = null;
            String count = null;
            Object value = object.get("exception");
            if (value != null) {
                exception = value.toString();
            }
            value = object.get("count");
            if (value != null) {
                count = value.toString();
            }
            exceptionBean.setClassName(className);
            exceptionBean.setException(exception);
            exceptionBean.setCount(Integer.valueOf(count));
            exceptionBeans.add(exceptionBean);
        }

        return exceptionBeans;
    }
}
