package com.home.logs.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.home.logs.fancylog.domain.LogKey;
import com.home.logs.fancylog.domain.Offer;
import com.home.logs.fancylog.utils.Utils;

@Component(value = "GetSeatOffer")
public class GetSeatOffer extends LogAnalyzer {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(GetSeatOffer.class);

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
            value = xPath.compile("/Envelope/Body/GetSeatOfferRequest/context/host").evaluate(doc);
            if (value != null && value.length() > 0) {
                host = value;
            }
            value = xPath.compile("/Envelope/Body/GetSeatOfferRequest/context/channel").evaluate(doc);
            if (value != null && value.length() > 0) {
                channel = value;
            }
            value = xPath.compile("/Envelope/Body/GetSeatOfferRequest/context/market").evaluate(doc);
            if (value != null && value.length() > 0) {
                market = value;
            }
            value = xPath.compile("/Envelope/Body/GetSeatOfferRequest/airProduct/reservation/reservationIdentifier").evaluate(doc);
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
            APPLICATION_LOGGER.error("Exception occured while parsing GetSeatOfferRequest {}", e.getStackTrace());
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
            value = xPath.compile("/Envelope/Body/GetSeatOfferResponse/errorItem/errorCode").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setErrorCode(value);
            }
            value = xPath.compile("/Envelope/Body/GetSeatOfferResponse/errorItem/errorText").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey.setErrorDescription(value);
            }

        }
        catch (Exception exception) {
            APPLICATION_LOGGER.debug(xmlPayload);
            APPLICATION_LOGGER.error("Exception occured while parsing GetSeatOfferResponse {}", exception.getStackTrace());
        }

        return logKey;
    }

}
