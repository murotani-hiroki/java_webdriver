package com.mrtn.scraping.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
public class ScrapingService {

    private Logger logger = LoggerFactory.getLogger(ScrapingService.class);
    
    public List<String> getGoodsInfo(String url) {
        logger.info("--- getGoodsInfo start ---");
        List<Map<String, String>> yahooInfos = getYafooInfo(url);

        List<String> results = new ArrayList<>(Arrays.asList("Yahoo JAN,Yahoo販売価格,ASIN,Amazon販売価格"));
        for (Map<String, String> yahooInfo : yahooInfos) {
            
            String janCode = yahooInfo.get("janCode");
            if (!StringUtils.hasText(janCode)) {
                continue;
            }

            Map<String, String> amazonInfo = getAmazonInfo(janCode);
            results.add(String.join(",", new String[]{ 
                yahooInfo.get("janCode"), yahooInfo.get("price"), amazonInfo.get("asin"), amazonInfo.get("price")  
            }));
        }
        logger.info("--- getGoodsInfo end ---");

        return results;
    }
    
    private ChromeDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        return new ChromeDriver(options);
    }

    private List<Map<String, String>> getYafooInfo(String url) {
        WebDriver driver = createWebDriver();

        driver.get(url);
        String shopName = driver.findElement(By.cssSelector("div.mdBreadCrumb li:first-child span")).getText();
        List<WebElement> items = driver.findElements(By.cssSelector("div#itmlst>ul>li"));

        List<Map<String, String>> yahooInfos = new ArrayList<>();

        //String price = null;
        for (WebElement item : items) {
            String detailUrl = item.findElement(By.cssSelector("div.elName>a")).getAttribute("href");
            String priceStr = item.findElement(By.cssSelector("span.elPriceValue")).getText();
            String price = priceStr.replaceAll(",", "").replaceAll("円", "");
            yahooInfos.add(new HashMap<String, String>(){{ put("shop", shopName); put("url", detailUrl); put("price", price); }});
        }

        for (Map<String, String> yahooInfo : yahooInfos) {
            driver.get(yahooInfo.get("url"));
            List<WebElement> elRows = driver.findElements(By.cssSelector("div#itm_cat li.elRow"));
            yahooInfo.put("janCode", "");
            for (WebElement elRow: elRows) {
                String title = elRow.findElement(By.cssSelector("div.elRowTitle > p")).getText();
                if (title.startsWith("JAN")) {
                    String janCode = elRow.findElement(By.cssSelector("div.elRowData > p")).getText();
                    yahooInfo.put("janCode", janCode);
                } 
            }
            logger.info(String.format("%s : janCode=%s", yahooInfo.get("shop"), yahooInfo.get("janCode")));
        }
        driver.quit();
        
        return yahooInfos;
    }
    
    private Map<String, String> getAmazonInfo(String janCode) {
        if (!StringUtils.hasText(janCode)) {
            return new HashMap<>(){{ put("asin", ""); put("price", ""); }};
        }
        
        WebDriver driver = createWebDriver();
        driver.get("https://www.amazon.co.jp");
        WebElement input = driver.findElement(By.name("field-keywords"));
        input.sendKeys(janCode);
        input.submit();

        WebElement item = null;
        try {
            item = driver.findElement(By.cssSelector("div.s-asin"));
            if (item == null) {
                driver.quit();
                return new HashMap<>(){{ put("asin", ""); put("price", ""); }};
            }
        } catch(Exception e) {
            logger.info("div.s-asin not found.");
            driver.quit();
            return new HashMap<>(){{ put("asin", ""); put("price", ""); }};
        }
    
        String asin = item.getAttribute("data-asin");
        logger.info(String.format("asin=%s",asin));

        item = driver.findElement(By.cssSelector("div.s-result-item div.sg-col-inner .a-price-whole"));
        if (item != null) {
            String price = item.getText().replaceAll("¥|￥|円", "");
            driver.quit();
            return new HashMap<>(){{ put("asin", asin); put("price", price); }};
        } else {
            driver.quit();
            return new HashMap<>(){{ put("asin", asin); put("price", ""); }};
        }
    }

}
