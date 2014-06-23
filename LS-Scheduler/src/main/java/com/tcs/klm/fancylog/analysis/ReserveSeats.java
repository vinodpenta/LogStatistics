package com.tcs.klm.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.Utils;

@Component(value = "ReserveSeats")
public class ReserveSeats extends LogAnalyzer {

    @Override
    public List<LogKey> getLogKeyFromRequest(String xmlPayload) {
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
            value = xPath.compile("/Envelope/Body/ReserveSeatsRequest/context/host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/ReserveSeatsRequest/context/channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/ReserveSeatsRequest/context/market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            value = xPath.compile("/Envelope/Body/ReserveSeatsRequest/passengerSegment/reservationIdentifier").evaluate(doc);
            if (value != null && value.length() > 0) {
                LogKey logKey = new LogKey();
                logKey.setPNR(value);
                logKey.setChannel(channel);
                logKey.setHost(host);
                logKey.setMarket(market);
                logKey.setServiceName("ReserveSeats");
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
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        String value = null;
        LogKey logKey = null;
        try {
            String xmlPayloadNameSpaceRemoved = "<?xml version='1.0' encoding='UTF-8'?>" + Utils.removeNameSpace(xmlPayload);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlPayloadNameSpaceRemoved)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            value = xPath.compile("/Envelope/Body/ReserveSeatsResponse/errorItem/errorCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorCode(value);
            }
            value = xPath.compile("/Envelope/Body/ReserveSeatsResponse/errorItem/errorText").evaluate(doc);
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
