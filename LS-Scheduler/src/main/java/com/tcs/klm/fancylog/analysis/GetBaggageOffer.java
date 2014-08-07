package com.tcs.klm.fancylog.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

import com.tcs.klm.fancylog.domain.LogKey;
import com.tcs.klm.fancylog.utils.Utils;

@Component(value = "GetBaggageOffer")
public class GetBaggageOffer extends LogAnalyzer {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(GetBaggageOffer.class);

    @Override
    public List<LogKey> getLogKeyFromRequest(String lineText,MongoTemplate mongoTemplate) {
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
                logKey.setServiceName("GetBaggageOffer");
                lstLogKey.add(logKey);
            }

        }
        catch (Exception e) {
            APPLICATION_LOGGER.error("Exception occured while parsing GetBaggageOfferRequest {}", e.getStackTrace());
        }
        return lstLogKey;
    }

    @Override
    public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate) {
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
            APPLICATION_LOGGER.debug(xmlPayload);
            APPLICATION_LOGGER.error("Exception occured while parsing GetBaggageOfferResponse {}", exception.getStackTrace());
        }

        return logKey;
    }

}
