package com.tcs.klm.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tcs.klm.web.services.ExceptionService;

@Controller
@RequestMapping(value = "/exceptions")
public class ExceptionController {

    @Autowired
    private ExceptionService exceptionService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String list(@RequestParam
    String date) {
        Map<String, Object> searchResultsMap = new HashMap<String, Object>();
        searchResultsMap.put("Records", exceptionService.list(date));
        searchResultsMap.put("Result", "OK");
        return "exceptions.jsp";
    }

}
