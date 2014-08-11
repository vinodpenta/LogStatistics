package com.tcs.klm.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tcs.klm.fancylog.domain.Report;
import com.tcs.klm.web.services.ReportsService;

@Controller
@RequestMapping(value = "/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Report> reports = new ArrayList<Report>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -100);

        /*
         * for (int i = 0; i < 100; i++) { Report report = new Report(); calendar.add(Calendar.HOUR_OF_DAY, 1);
         * 
         * report.setDate(calendar.getTime()); report.setDoubleSeat(2 * Integer.valueOf(Math.round(Math.random() * 100) + "")); report.setProductName(100); report.setProductType(150);
         * System.out.println(report.getDate()); reports.add(report); }
         */

        reports = reportsService.list();
        model.addAttribute("data", reports);
        return "../view/reports.jsp";
    }
}
