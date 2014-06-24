package com.tcs.klm.fancylog.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tcs.klm.fancylog.domain.ExceptionKey;

@Component
@Scope("prototype")
public class ExceptionAnalysisThread implements Runnable {

    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(ExceptionAnalysisThread.class);

    private File file;
    private String sessionIDPossition;
    private MongoTemplate mongoTemplate;

    private String COLLECTION_EXCEPTION = "exception";

    public ExceptionAnalysisThread(File file, String sessionIDPossition, MongoTemplate mongoTemplate) {
        this.file = file;
        this.mongoTemplate = mongoTemplate;
        this.sessionIDPossition = sessionIDPossition;
    }

    @Override
    public void run() {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = calendar.get(Calendar.YEAR) + "";
            if (file != null) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuffer sbf = new StringBuffer();
                String sCurrentLine = null;
                while ((sCurrentLine = br.readLine()) != null) {
                    if (sCurrentLine.startsWith(year)) {
                        try {
                            processLastLine(sbf.toString(), year, file.getName());
                        }
                        catch (Exception e) {
                            APPLICATION_LOGGER.error(sbf.toString());
                            APPLICATION_LOGGER.error(e.getMessage());
                        }
                        sbf.delete(0, sbf.length());
                        sbf.append(sCurrentLine + "\n");
                    }
                    else {
                        sbf.append(sCurrentLine + "\n");
                    }
                }
                br.close();
            }
            file.delete();
        }
        catch (Exception e) {
            APPLICATION_LOGGER.error("Exception {} occured while analysing {}", e, file.getName());
        }

    }

    private void processLastLine(String lineText, String year, String name) {
        String sessionID = null;
        String className = null;
        String exception = null;
        String errorDescription = null;
        if (lineText.startsWith(year)) {
            sessionID = getString(lineText, Integer.valueOf(sessionIDPossition));
            className = getString(lineText, 4);
            exception = getexception(lineText);
            errorDescription = getErrorDescription(lineText);

            ExceptionKey exceptionKey = new ExceptionKey();
            exceptionKey.setLog(lineText);
            exceptionKey.setClassName(className);
            exceptionKey.setException(exception);
            exceptionKey.setSessionID(sessionID);
            exceptionKey.setErrorDescription(errorDescription);

            Date today = Calendar.getInstance().getTime();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(today);
            exceptionKey.setDate(date);
            mongoTemplate.insert(exceptionKey, COLLECTION_EXCEPTION);
        }
    }

    private String getErrorDescription(String lineText) {
        String firstLine = lineText.substring(121);
        String strs[] = StringUtils.split(firstLine, "\n");
        String errorDescription = strs[0];
        return errorDescription;
    }

    private String getexception(String lineText) {
        String strs[] = StringUtils.split(lineText, "\n");
        String exception = strs[1];
        return exception;
    }

    private String getString(String lineText, Integer possition) {
        String sessionID = new String();
        String strs[] = lineText.split(" ");
        sessionID = strs[Integer.valueOf(possition) - 1];
        return sessionID;
    }

}
