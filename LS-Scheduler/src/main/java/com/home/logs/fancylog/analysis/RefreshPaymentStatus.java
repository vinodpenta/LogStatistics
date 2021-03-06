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

@Component(value = "RefreshPaymentStatus")
public class RefreshPaymentStatus extends LogAnalyzer {

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
            value = xPath.compile("/Envelope/Body/RefreshPaymentStatusRequest/Context/Host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/RefreshPaymentStatusRequest/Context/Channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/RefreshPaymentStatusRequest/Context/Market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            LogKey logKey = new LogKey();
            logKey.setChannel(channel);
            logKey.setHost(host);
            logKey.setMarket(market);
            logKey.setServiceName("RefreshPaymentStatus");
            lstLogKey.add(logKey);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return lstLogKey;
    }

    @Override
    public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate, Map<Offer, Integer> offerMap) {
        String xmlPayload = lineText.substring(lineText.indexOf("<?xml version="));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        String value = null;
        LogKey logKey = null;
        try {
            String xmlPayloadNameSpaceRemoved = "<?xml version='1.0' encoding='UTF-8'?>" + Utils.removeNameSpace(xmlPayload);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlPayloadNameSpaceRemoved)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            value = xPath.compile("/Envelope/Body/RefreshPaymentStatusResponse/ErrorItem/ErrorCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorCode(value);
            }
            value = xPath.compile("/Envelope/Body/RefreshPaymentStatusResponse/ErrorItem/ErrorText").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey.setErrorDescription(value);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return logKey;
    }

}
