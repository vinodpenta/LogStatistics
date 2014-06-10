package com.tcs.klm.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tcs.klm.web.domain.SettingsBean;
import com.tcs.klm.web.services.SettingsService;

@Controller
@RequestMapping(value = "/settings", produces = "application/json")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> list() {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        List<SettingsBean> settings = new ArrayList<SettingsBean>();
        settings.addAll(settingsService.list());
        settingsMap.put("Records", settings);
        settingsMap.put("Result", "OK");
        return settingsMap;
    }

    @RequestMapping(value = "/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam
    String id) {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        settingsService.delete(id);
        settingsMap.put("Result", "OK");
        return settingsMap;
    }

    @RequestMapping(value = "/new")
    @ResponseBody
    public Map<String, Object> create(SettingsBean setting) {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        settingsMap.put("Record", settingsService.save(setting));
        settingsMap.put("Result", "OK");
        return settingsMap;
    }

    @RequestMapping(value = "/save")
    @ResponseBody
    public Map<String, Object> update(@ModelAttribute
    SettingsBean setting) {
        Map<String, Object> settingsMap = new HashMap<String, Object>();
        settingsMap.put("Record", settingsService.save(setting));
        settingsMap.put("Result", "OK");
        return settingsMap;
    }
}
