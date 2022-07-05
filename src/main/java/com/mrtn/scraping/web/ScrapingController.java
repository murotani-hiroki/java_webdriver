package com.mrtn.scraping.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.mrtn.scraping.service.ScrapingService;

@Controller
public class ScrapingController {
    
    @Autowired
    private ScrapingService scrapingService;

    @RequestMapping(value = "/")
    public ModelAndView top(ModelAndView mv) {
        mv.setViewName("index");
        return mv;
    }

    @RequestMapping(value = "/download")
    @ResponseBody
    public String download(@RequestParam String url, HttpServletResponse response) {

        List<String> results = scrapingService.getGoodsInfo(url);

        try (OutputStream os = response.getOutputStream()) {
            byte[] resultsBytes = String.join("\n", results).getBytes();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment: filename=foo.csv");
            response.setContentLength(resultsBytes.length);
            os.write(resultsBytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    
}
