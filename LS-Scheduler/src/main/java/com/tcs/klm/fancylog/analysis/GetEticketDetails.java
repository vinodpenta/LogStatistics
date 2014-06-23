package com.tcs.klm.fancylog.analysis;

import java.io.StringReader;
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

@Component(value = "GetEticketDetails")
public class GetEticketDetails extends LogAnalyzer {

    @Override
    public List<LogKey> getLogKeyFromRequest(String xmlPayload) {
        // TODO Auto-generated method stub
        return null;
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
            value = xPath.compile("/Envelope/Body/GetEticketDetailsResponse/TicketDetails/PNRLocator/@BookingReferenceID").evaluate(doc);
            if (value != null && value.length() > 0) {
                logKey = new LogKey();
                logKey.setPNR(value);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return logKey;
    }
}
