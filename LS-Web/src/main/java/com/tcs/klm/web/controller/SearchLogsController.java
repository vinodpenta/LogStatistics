package com.tcs.klm.web.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tcs.klm.web.services.SearchLogsService;

@Controller
@RequestMapping(value = "/searchLogs")
public class SearchLogsController {

    @Autowired
    private SearchLogsService searchLogsService;

    @Autowired
    // Must needed
    ServletContext context; // Must needed

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> list(@RequestParam
    String pnr) {
        Map<String, Object> searchResultsMap = new HashMap<String, Object>();
        searchResultsMap.put("Records", searchLogsService.searchResults(pnr));
        searchResultsMap.put("Result", "OK");
        return searchResultsMap;
    }

    @RequestMapping(value = "/logs")
    public String logs(@RequestParam
    final String id, ModelMap model) throws IOException {
        model.addAttribute("logs", searchLogsService.getLogs(id));
        return "/view/log.jsp";
    }

    @RequestMapping(value = "/download")
    public void download(@RequestParam
    final String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String log = searchLogsService.getLogs(id);
        File file = new File("C:/temp/completelogs.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        bw.append(log + "\n");
        bw.close();
        FileInputStream inputStream = new FileInputStream(file);

        // get MIME type of the file
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String mimeType = mimeTypesMap.getContentType(file);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) file.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

    }

}
