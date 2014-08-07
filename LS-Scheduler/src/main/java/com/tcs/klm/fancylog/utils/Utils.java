package com.tcs.klm.fancylog.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public class Utils {

    public static String extractXPATHSource(String xmlPayloadSource) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String leftXPATHSource = null;
        if (xmlPayloadSource != null) {
            StringWriter writerNamespaceremoved = applyXSLTransformation(xmlPayloadSource, "C:\\dev\\tools\\removenamespace.xsl");
            StringWriter writer = applyXSLTransformation(writerNamespaceremoved.toString(), "C:\\dev\\tools\\xpath.xsl");
            leftXPATHSource = writer.toString();

        }
        return leftXPATHSource;
    }

    public static String removeNameSpace(String xmlPayload) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String leftXPATHSource = null;
        if (xmlPayload != null) {
            StringWriter writerNamespaceremoved = applyXSLTransformation(xmlPayload, "C:\\dev\\tools\\removenamespace.xsl");
            leftXPATHSource = writerNamespaceremoved.toString();

        }
        return leftXPATHSource;

    }

    private static StringWriter applyXSLTransformation(String xmlPayloadNamespaceRemoved, String filePathRemoveNamespaceXSL) throws TransformerFactoryConfigurationError, TransformerConfigurationException,
                    TransformerException {
        StringReader reader = new StringReader(xmlPayloadNamespaceRemoved);
        StringWriter writer = new StringWriter();
        javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(reader);
        javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(filePathRemoveNamespaceXSL);
        javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(writer);
        javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
        trans.transform(xmlSource, result);
        return writer;
    }

    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        String outStr = out.toString("ISO-8859-1");
        return outStr;
    }

    public static String decompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        return outStr;
    }

}
