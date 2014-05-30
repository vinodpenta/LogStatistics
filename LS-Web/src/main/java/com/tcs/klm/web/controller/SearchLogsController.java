package com.tcs.klm.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/searchLogs")
public class SearchLogsController {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> list(@RequestParam
    String pnr) {
        Map<String, Object> searchResultsMap = new HashMap<String, Object>();
        searchResultsMap.put("Records", null);
        System.out.println("searchResultsMap");
        searchResultsMap.put("Result", "OK");
        return searchResultsMap;
    }
}
