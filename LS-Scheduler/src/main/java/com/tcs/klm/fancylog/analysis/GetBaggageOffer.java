package com.tcs.klm.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.Utils;

@Component(value = "GetBaggageOffer")
public class GetBaggageOffer extends LogAnalyzer {

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    @Override
    public List<LogKey> getLogKeyFromRequest(String xmlPayload) {
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
            value = xPath.compile("/Envelope/Body/GetBaggageOfferRequest/Context/Host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/GetBaggageOfferRequest/Context/Channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/GetBaggageOfferRequest/Context/Market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            value = xPath.compile("/Envelope/Body/GetBaggageOfferRequest/Reservation/PNRAddress").evaluate(doc);
            if (value != null && value.length() > 0) {
                LogKey logKey = new LogKey();
                logKey.setPNR(value);
                logKey.setChannel(channel);
                logKey.setHost(host);
                logKey.setMarket(market);
                logKey.setServiceName("GetSeatOffer");
                lstLogKey.add(logKey);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return lstLogKey;
    }

    @Override
    public LogKey getLogKeyFromResponse(String xmlPayload) {
        String value = null;
        LogKey logKey = null;
        try {
            String xmlPayloadNameSpaceRemoved = "<?xml version='1.0' encoding='UTF-8'?>" + Utils.removeNameSpace(xmlPayload);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlPayloadNameSpaceRemoved)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            value = xPath.compile("/Envelope/Body/GetBaggageOfferResponse/ErrorItem/ErrorCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorCode(value);
            }
            value = xPath.compile("/Envelope/Body/GetBaggageOfferResponse/ErrorItem/ErrorText").evaluate(doc);
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
