package com.tcs.klm.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.Utils;

@Component(value = "ListAvailableProducts")
public class ListAvailableProducts extends LogAnalyzer {
    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(ListAvailableProducts.class);

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
            value = xPath.compile("/Envelope/Body/ListAvailableProductsRequest/context/host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/ListAvailableProductsRequest/context/channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/ListAvailableProductsRequest/context/market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            NodeList reservationNode = (NodeList) xPath.compile("/Envelope/Body/ListAvailableProductsRequest/airProduct/reservation").evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < reservationNode.getLength(); i++) {
                value = xPath.compile("/Envelope/Body/ListAvailableProductsRequest/airProduct/reservation[" + (i + 1) + "]/reservationIdentifier").evaluate(doc);
                if (value != null && value.length() > 0) {
                    LogKey logKey = new LogKey();
                    logKey.setPNR(value);
                    logKey.setChannel(channel);
                    logKey.setHost(host);
                    logKey.setMarket(market);
                    logKey.setServiceName("ListAvailableProducts");
                    lstLogKey.add(logKey);
                }
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
            value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/errorItem/errorCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorCode(value);
            }
            value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/errorItem/errorText").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey.setErrorDescription(value);
            }

            value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/warningItem/warningText").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorDescription(value);
            }
            value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/warningItem/warningCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey.setErrorCode(value);
            }
        }
        catch (Exception exception) {
            APPLICATION_LOGGER.debug(xmlPayload);
            APPLICATION_LOGGER.error("Exception occured while parsing listAvailableProductsSuccessResponse {}", exception.getStackTrace());
        }

        return logKey;
    }
}
