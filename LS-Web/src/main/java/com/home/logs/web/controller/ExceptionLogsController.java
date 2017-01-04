package com.home.logs.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.home.logs.web.services.ExceptionLogService;

@Controller
@RequestMapping(value = "/exceptionlogs")
public class ExceptionLogsController {

    @Autowired
    private ExceptionLogService exceptionLogService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> list(@RequestParam
    String id) {
        Map<String, Object> searchResultsMap = new HashMap<String, Object>();
        searchResultsMap.put("Records", exceptionLogService.list(id));
        searchResultsMap.put("Result", "OK");
        return searchResultsMap;
    }

}
