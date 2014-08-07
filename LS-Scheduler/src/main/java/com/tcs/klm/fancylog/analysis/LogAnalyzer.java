package com.tcs.klm.fancylog.analysis;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.tcs.klm.fancylog.domain.LogKey;

public abstract class LogAnalyzer {
    abstract public List<LogKey> getLogKeyFromRequest(String lineText, MongoTemplate mongoTemplate);

    abstract public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate);

}
