package com.tcs.klm.web.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tcs.klm.web.domain.SettingsBean;

@Component
public class SettingsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String COLLECTION_NAME = "settings";

    public SettingsBean save(SettingsBean setting) {
        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            mongoTemplate.createCollection(COLLECTION_NAME);
        }
        if (setting.getId() == null) {
            setting.setId(UUID.randomUUID().toString());
            mongoTemplate.insert(setting, COLLECTION_NAME);
        }
        else {
            mongoTemplate.save(setting, COLLECTION_NAME);
        }

        Query query = new Query(Criteria.where("_id").is(setting.getId()));
        SettingsBean settingsBean = (SettingsBean) mongoTemplate.findOne(query, SettingsBean.class, COLLECTION_NAME);

        return settingsBean;
    }

    public List<SettingsBean> list() {
        List<SettingsBean> settingss = mongoTemplate.findAll(SettingsBean.class);

        return settingss;
    }

    public void delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, COLLECTION_NAME);
    }
}
