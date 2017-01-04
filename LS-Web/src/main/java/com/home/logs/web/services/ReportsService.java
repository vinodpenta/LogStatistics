package com.home.logs.web.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.home.logs.fancylog.domain.Report;

@Component
public class ReportsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Report> list() {
        List<Report> reports = new ArrayList<Report>();
        Map<Date, Report> reportsMap = new TreeMap<Date, Report>();
        DBCollection offerCollection = mongoTemplate.getCollection("offer");

        BasicDBObject sortOrder = new BasicDBObject();
        sortOrder.put("date", 1); // order ASC

        DBCursor cursor = offerCollection.find();// .sort(sortOrder);
        while (cursor.hasNext()) {
            DBObject dbObject = cursor.next();
            Date date = (Date) dbObject.get("date");
            Report report = reportsMap.get(date);
            if (report == null) {
                report = new Report();
                report.setDate(date);
                reportsMap.put(date, report);
            }
            Integer count = null;
            String value = (String) dbObject.get("productName");
            if (value != null && value.equalsIgnoreCase("Y35")) {
                count = (Integer) dbObject.get("count");
                report.setY35(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("EUREXTRALEGROOML")) {
                count = (Integer) dbObject.get("count");
                report.setEurExtraLegRoomL(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("WWPIECE")) {
                count = (Integer) dbObject.get("count");
                report.setWwPeice(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("AFPIECEONLINE")) {
                count = (Integer) dbObject.get("count");
                report.setAfPeiceOnline(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("CO2COMPENSATIONPRODUCTCATEGORY")) {
                count = (Integer) dbObject.get("count");
                report.setCo2(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("SPECIALMEAL")) {
                count = (Integer) dbObject.get("count");
                report.setSpecialMeal(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("DOUBLESEATS")) {
                count = (Integer) dbObject.get("count");
                report.setDoubleSeat(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("ICAEXTRALEGROOMS")) {
                count = (Integer) dbObject.get("count");
                report.setIcaExtraLegRoomS(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("PAIDUPGRADEKL")) {
                count = (Integer) dbObject.get("count");
                report.setPaidUpgradeKL(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("EUREXTRALEGROOMS")) {
                count = (Integer) dbObject.get("count");
                report.setEurExtraLegRoomS(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("ICAEXTRALEGROOML")) {
                count = (Integer) dbObject.get("count");
                report.setIcaExtraLegRoomL(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("STANDARDMEAL")) {
                count = (Integer) dbObject.get("count");
                report.setStandradMeal(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("SEATCHOICECATEGORYAF")) {
                count = (Integer) dbObject.get("count");
                report.setSeatChoiceCategoryAF(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("AFPIECE")) {
                count = (Integer) dbObject.get("count");
                report.setAfPeice(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("SEATDUO")) {
                count = (Integer) dbObject.get("count");
                report.setSeatDuo(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("SEATFRONT")) {
                count = (Integer) dbObject.get("count");
                report.setSeatFront(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("PAIDUPGRADEAFEBMEDIUMHAUL")) {
                count = (Integer) dbObject.get("count");
                report.setPaidUpgradeAFEBMediumhaul(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("DLECONOMYCOMFORT")) {
                count = (Integer) dbObject.get("count");
                report.setDlEconomyComfort(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("PAIDUPGRADEAFEB")) {
                count = (Integer) dbObject.get("count");
                report.setPaidUpgradeAFEB(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("ALACARTE")) {
                count = (Integer) dbObject.get("count");
                report.setAlaCarte(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("PAIDUPGRADEAFEP")) {
                count = (Integer) dbObject.get("count");
                report.setPaidUpgradeAFEP(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("SEATUPPERDECK")) {
                count = (Integer) dbObject.get("count");
                report.setSeatUperDeck(Integer.valueOf(count));
            }
            else if (value != null && value.equalsIgnoreCase("PAIDUPGRADEAFPB")) {
                count = (Integer) dbObject.get("count");
                report.setPaidUpgradeAFPB(Integer.valueOf(count));
            }

            String productType = (String) dbObject.get("productType");
            if (productType != null && productType.equalsIgnoreCase("PAIDUPGRADEAF")) {
                count = (Integer) dbObject.get("count");
                report.setAfPU(report.getAfPU() + count);
            }
            else if (productType != null && productType.equalsIgnoreCase("PAIDSEAT")) {
                count = (Integer) dbObject.get("count");
                report.setPaidSeats(report.getPaidSeats() + count);
            }
            else if (productType != null && productType.equalsIgnoreCase("AFABA")) {
                count = (Integer) dbObject.get("count");
                report.setAfABA(report.getAfABA() + count);
            }
            else if (productType != null && productType.equalsIgnoreCase("SEATCHOICEAF")) {
                count = (Integer) dbObject.get("count");
                report.setSeatChoiceAF(report.getSeatChoiceAF() + count);
            }

        }
        reports = new ArrayList<Report>(reportsMap.values());
        return reports;
    }
}
