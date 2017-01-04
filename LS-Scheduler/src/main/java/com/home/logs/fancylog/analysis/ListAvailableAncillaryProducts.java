package com.home.logs.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.home.logs.fancylog.domain.LogKey;
import com.home.logs.fancylog.domain.Offer;
import com.home.logs.fancylog.utils.Utils;

@Component(value = "ListAvailableAncillaryProducts")
public class ListAvailableAncillaryProducts extends LogAnalyzer {

    @Override
    public List<LogKey> getLogKeyFromRequest(String lineText, MongoTemplate mongoTemplate) {
        String xmlPayload = lineText.substring(lineText.indexOf("<?xml version="));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        String value = null;
        List<LogKey> lstLogKey = new ArrayList<LogKey>();
        try {
            String xmlPayloadNameSpaceRemoved = "<?xml version='1.0' encoding='UTF-8'?>" + Utils.removeNameSpace(xmlPayload);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlPayloadNameSpaceRemoved)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            String host = null;
            String channel = null;
            String market = null;
            value = xPath.compile("/Envelope/Body/ListAvailableAncillaryProductsRequest/context/host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/ListAvailableAncillaryProductsRequest/context/channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/ListAvailableAncillaryProductsRequest/context/market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            value = xPath.compile("/Envelope/Body/ListAvailableAncillaryProductsRequest/airProduct/reservations/reservationId").evaluate(doc);
            if (value != null && value.length() > 0) {
                LogKey logKey = new LogKey();
                logKey.setPNR(value);
                logKey.setChannel(channel);
                logKey.setHost(host);
                logKey.setMarket(market);
                logKey.setServiceName("ListAvailableAncillaryProducts");
                lstLogKey.add(logKey);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return lstLogKey;
    }

    @Override
    public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate, Map<Offer, Integer> offerMap) {
        // TODO Auto-generated method stub
        return null;
    }

}
