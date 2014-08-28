package com.tcs.klm.fancylog.analysis;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.domain.Offer;

public abstract class LogAnalyzer {
    abstract public List<LogKey> getLogKeyFromRequest(String lineText, MongoTemplate mongoTemplate);

    abstract public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate, Map<Offer, Integer> offerMap);

}
