package com.tcs.klm.fancylog.analysis;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.LogAnalyzer;

@Component(value = "ListAvailableProducts")
public class ListAvailableProducts extends LogAnalyzer {

    @Override
    public List<LogKey> getLogKeyFromRequest(String xmlPayload) {
        return null;
    }

    @Override
    public LogKey getLogKeyFromResponse(String xmlPayload) {
        return null;
    }

}
