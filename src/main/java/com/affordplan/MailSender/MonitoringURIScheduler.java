package com.affordplan.MailSender;

import com.affordplan.Model.Data;
import com.affordplan.Model.UriProperties;
import com.google.gson.Gson;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@PropertySource("classpath:application.properties")
public class MonitoringURIScheduler {

    public final static int COUNTER_LIMIT = 3;
    private static final Logger LOG = Logger.getLogger(MonitoringURIScheduler.class);
    private static final String SUCCESS_CODE = "200 OK";
    private static final String SUCCESS_MESSAGE = "OperationSuccess";
    UriProperties uriPropertiesOne;
    UriProperties uriPropertiesTwo;
    UriProperties uriPropertiesThree;
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${uri.var1}")
    private String uri1;
    @Value("${uri.var2}")
    private String uri2;
    @Value("${uri.var3}")
    private String uri3;
    @Value("${message.setTo}")
    private String setToEmail;



    @Value("${Timer.time}")
    private int time;

    @Value("${ThreshHold.time}")
    private int threshHold;

    private Map<String, UriProperties> uriMap;

    @PostConstruct
    public void createHashMap() {

        uriPropertiesOne = new UriProperties(uri1, 0);
        uriPropertiesTwo = new UriProperties(uri2, 0);
        uriPropertiesThree = new UriProperties(uri3, 0);
        uriMap = new HashMap<String, UriProperties>() {{
            put(uri1, uriPropertiesOne);
            put(uri2, uriPropertiesTwo);
            put(uri3, uriPropertiesThree);
        }};


    }

    public void sendEmail(String subject, String content) {
        LOG.debug("Inside Send Email");
        try {
            String[] emailRecipients = setToEmail.split(",");
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailRecipients);
            msg.setSubject(subject);
            msg.setText(content);
            javaMailSender.send(msg);
        } catch (Exception e) {
            LOG.error("Error in sendEmail "+e);
        }

    }

    public ResponseEntity<String> checkResponseStatus(String url) {
        LOG.debug("Inside checkResponseStatus with url "+url);
       // log.logger(url,"insideCheckResponse");
        ResponseEntity<String> output;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        long time = System.currentTimeMillis();
        output = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        long responseTime = (System.currentTimeMillis() - time);

        if(responseTime>threshHold){

            LOG.info("Response time for "+ url + "is long with response time "+responseTime);

            sendEmail("server " + url + "slow response time", "server instance of " + url + " taking long response time");

        }

        LOG.info("Response time "+url+ "  "+responseTime);

        LOG.info("Status code "+url+"  "+output);

        return output;
    }

    public void SchedulingMailingEvent(UriProperties uriproperties) {
        LOG.debug("Inside SchedulingMailingEvent for uri " + uriproperties.getUri());

        ResponseEntity<String> output;
        int count;
        try {
            output = checkResponseStatus(uriproperties.getUri());
            String statusCode = String.valueOf(output.getStatusCode());
            Gson g = new Gson();
            Data outputBody = g.fromJson(output.getBody(), Data.class);

            if (!(statusCode.equals(SUCCESS_CODE)) && !(outputBody.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of server " + uriproperties.getUri() + " down");
                sendEmail("server " + uriproperties.getUri() + " down", "server instance of " + uriproperties.getUri() + " down");
                LOG.info("Mail sent for uri " + uriproperties.getUri());
            }
            else{
                if(uriproperties.getCounter()>0){
                    LOG.info("Server is Up for server " + uriproperties.getUri());
                    sendEmail("server " + uriproperties.getUri() + " is up", "server instance of " + uriproperties.getUri() + " is up");
                    LOG.info("Mail sent for server up for uri " + uriproperties.getUri());
                    uriproperties.setCounter(0);
                }
            }


        } catch (Exception e) {
            LOG.debug("Inside catch of SchedulingMailingEvent ");
            LOG.error("server instance " + uriproperties.getUri() + " down");
             count = uriproperties.getCounter();
             count++;
            uriproperties.setCounter(count);
            if(uriproperties.getCounter()>0 && ((uriproperties.getCounter()<3 ) || (uriproperties.getCounter()%5==0))){
                LOG.debug("Mail Sent condition satisfied ");
                sendEmail("server " + uriproperties.getUri() + " down", "server instance of " + uriproperties.getUri() + " down");
            }

        }

    }

    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventOne() {
        LOG.debug("Inside SchedulingMailingEventOne");

        SchedulingMailingEvent(uriPropertiesOne);

    }

    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventTwo() {
        LOG.debug("Inside SchedulingMailingEventTwo");

        SchedulingMailingEvent(uriPropertiesTwo);
    }


    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventThree() {
        LOG.debug("Inside SchedulingEventThree");

        SchedulingMailingEvent(uriPropertiesThree);

    }

    public static String createCSVFormatString(String inputStr) {
        String outputStr = "";
        if (inputStr != null) {
            outputStr = inputStr.replace("\"", "\"\"");
        }
        return outputStr;
    }




}
