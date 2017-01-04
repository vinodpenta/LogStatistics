package com.home.logs.fancylog.analysis;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.home.logs.fancylog.domain.LogKey;
import com.home.logs.fancylog.domain.Offer;
import com.home.logs.fancylog.utils.FancySharedInfo;
import com.home.logs.fancylog.utils.Utils;

@Component(value = "ListAvailableProducts")
public class ListAvailableProducts extends LogAnalyzer {
    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(ListAvailableProducts.class);

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
    public LogKey getLogKeyFromResponse(String lineText, MongoTemplate mongoTemplate, Map<Offer, Integer> offerMap) {
        String xmlPayload = lineText.substring(lineText.indexOf("<?xml version="));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        String value = null;
        LogKey logKey = null;
        String productType = null;
        String productName = null;
        String productClass = null;
        Date date = new Date();
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
            NodeList productNodeList = (NodeList) xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/availableProduct").evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < productNodeList.getLength(); i++) {
                Offer offer = new Offer();
                value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/availableProduct[" + (i + 1) + "]/productName").evaluate(doc);
                if (value != null && value.length() > 0) {
                    productName = value.replace(" ", "").replace("-", "").replace("_", "").toUpperCase();
                    offer.setProductName(productName);
                }
                value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/availableProduct[" + (i + 1) + "]/productType").evaluate(doc);
                if (value != null && value.length() > 0) {
                    productType = value.replace(" ", "").replace("-", "").replace("_", "").toUpperCase();
                    offer.setProductType(productType);
                }
                value = xPath.compile("/Envelope/Body/ListAvailableProductsResponse/listAvailableProductsSuccessResponse/availableProduct[" + (i + 1) + "]/productClass").evaluate(doc);
                if (value != null && value.length() > 0) {
                    productClass = value.replace(" ", "").replace("-", "").replace("_", "").toUpperCase();
                    offer.setProductClass(productClass);
                }
                if (productName != null && productType != null && productClass != null) {
                    String dateStr = FancySharedInfo.getInstance().getDate(lineText);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH");
                    try {
                        date = formatter.parse(dateStr);
                        offer.setDate(date);
                        Integer count = offerMap.get(offer);
                        if (count != null) {
                            count++;
                        }
                        else {
                            count = new Integer(1);
                            offerMap.put(offer, count);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception exception) {
            APPLICATION_LOGGER.debug(xmlPayload);
            APPLICATION_LOGGER.error("Exception occured while parsing listAvailableProductsSuccessResponse {}", exception.getStackTrace());
        }

        return logKey;
    }

    /*
     * public static void main(String ar[]) { try { File file = new File("C:/dev/vinod/temp.txt"); BufferedReader br = new BufferedReader(new FileReader(file)); StringBuffer sbf = new StringBuffer(); String sCurrentLine
     * = null;
     * 
     * while ((sCurrentLine = br.readLine()) != null) { if (sCurrentLine.startsWith("2014")) { try { getLogKeyFromResponse(sCurrentLine, null); } catch (Exception e) { APPLICATION_LOGGER.error(sbf.toString());
     * APPLICATION_LOGGER.error(e.getMessage()); } sbf.delete(0, sbf.length()); sbf.append(sCurrentLine); } else { sbf.append(sCurrentLine); } }
     * 
     * } catch (Exception exception) {
     * 
     * }
     * 
     * }
     */
}
