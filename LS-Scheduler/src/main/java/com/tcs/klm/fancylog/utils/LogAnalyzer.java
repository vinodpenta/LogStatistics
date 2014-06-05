package com.tcs.klm.fancylog.utils;

import java.util.List;

import com.tcs.klm.fancylog.domain.LogKey;

public abstract class LogAnalyzer {
    abstract public List<LogKey> getLogKeyFromRequest(String xmlPayload);

    abstract public LogKey getLogKeyFromResponse(String xmlPayload);

}
