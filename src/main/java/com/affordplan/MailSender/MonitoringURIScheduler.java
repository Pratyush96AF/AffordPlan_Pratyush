package com.affordplan.MailSender;

import com.affordplan.Model.Data;
import com.affordplan.Model.UriProperties;
import com.google.gson.Gson;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private Map<String, UriProperties> uriMap;

    @PostConstruct
    public void createHashMap() {

        uriPropertiesOne = new UriProperties(uri1, 3, 1, 1);
        uriPropertiesTwo = new UriProperties(uri2, 3, 1, 1);
        uriPropertiesThree = new UriProperties(uri3, 3, 1, 1);
        uriMap = new HashMap<String, UriProperties>() {{
            put(uri1, uriPropertiesOne);
            put(uri2, uriPropertiesTwo);
            put(uri3, uriPropertiesThree);
        }};


    }

    public void sendEmail(String subject, String content) {
        try {
            LOG.info("Sending Email");
            String[] emailRecipients = setToEmail.split(",");
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailRecipients);
            msg.setSubject(subject);
            msg.setText(content);
            javaMailSender.send(msg);
        } catch (Exception e) {
            LOG.error(e);
        }

    }


    @Scheduled(fixedRate = 120000)
    public void SchedulingMailingEventOne() {
        LOG.info("Scheduling Event for uri1");

        ResponseEntity<String> outOne;

        try {
            outOne = checkResponseStatus(uri1);
            String statusCodeOne = String.valueOf(outOne.getStatusCode());
            Gson g = new Gson();
            Data bodyOne = g.fromJson(outOne.getBody(), Data.class);
            if (!(statusCodeOne.equals(SUCCESS_CODE)) || !(bodyOne.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of staging server down");
                sendEmail("Staging server down", "server instance of staging server down");
                LOG.info("Mail sent for second uri status");
            }
            if ((uriMap.get(uri1).getCounter()) < COUNTER_LIMIT) {
                LOG.info("Server is Up for staging server");
                uriPropertiesOne.setCounter(3);
                uriPropertiesOne.setPhaseCounter(1);
                uriPropertiesOne.setMaxCounter(1);
                uriMap.put(uri1, uriPropertiesOne);
                sendEmail("staging server is up","server instance of staging is up ");
                LOG.info("Mail sent for staging  uri ");
            }
        } catch (Exception e) {
            LOG.error("server instance of staging server down");
            if ((uriMap.get(uri1).getPhaseCounter()) == (uriMap.get(uri1).getMaxCounter())) {
                int counter = uriMap.get(uri1).getCounter();
                if (counter == 0) {
                    int currentMaxCounter = (uriPropertiesOne.getMaxCounter()) * 2; //increaing counter by factor of 2
                    uriPropertiesOne.setMaxCounter(currentMaxCounter);
                    uriPropertiesOne.setPhaseCounter(1);
                    uriPropertiesOne.setCounter(3);
                    uriMap.put(uri1, uriPropertiesOne);
                }
                counter = uriPropertiesOne.getCounter();
                counter--;
                uriPropertiesOne.setCounter(counter);
                uriMap.put(uri1, uriPropertiesOne);
                sendEmail("the server with port 8080 down", "the server is down");
                LOG.info("Mail sent for staging  uri ");

            } else {
                int phaseCounter = uriMap.get(uri1).getPhaseCounter();
                phaseCounter++;
                uriPropertiesOne.setPhaseCounter(phaseCounter);
                uriMap.put(uri1, uriPropertiesOne);
            }

        }

    }

    @Scheduled(fixedRate = 120000)
    public void SchedulingMailingEventTwo() {
        LOG.info("Scheduling event for second uri");
        ResponseEntity<String> outTwo;

        try {
            outTwo = checkResponseStatus(uri2);
            String statusCodeTwo = String.valueOf(outTwo.getStatusCode());
            Gson g = new Gson();
            Data bodyTwo = g.fromJson(outTwo.getBody(), Data.class);
            if (!(statusCodeTwo.equals(SUCCESS_CODE)) || !(bodyTwo.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of localhost with port 8080  down");
                sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");
                LOG.info("Mail Sent for second uri");
            }

            if ((uriMap.get(uri2).getCounter()) < COUNTER_LIMIT) {
                LOG.info("Server is Up with port 8080");
                uriPropertiesTwo.setCounter(3);
                uriPropertiesTwo.setPhaseCounter(1);
                uriPropertiesTwo.setMaxCounter(1);
                uriMap.put(uri2, uriPropertiesTwo);
                sendEmail("localhost:8080 is up", "server instance of localhost with port 8080 up again ");
                LOG.info("Mail Sent for second uri");
            }

        } catch (Exception e) {
            LOG.error("server instance of localhost with port 8080  down");
            if ((uriMap.get(uri2).getPhaseCounter()) == (uriMap.get(uri2).getMaxCounter())) {
                int counter = uriMap.get(uri2).getCounter();
                if (counter == 0) {
                    int currentMaxCounter = (uriPropertiesTwo.getMaxCounter()) * 2; //increaing counter by factor of 2
                    uriPropertiesTwo.setMaxCounter(currentMaxCounter);
                    uriPropertiesTwo.setPhaseCounter(1);
                    uriPropertiesTwo.setCounter(3);
                    uriMap.put(uri2, uriPropertiesTwo);
                }
                counter = uriPropertiesTwo.getCounter();
                counter--;
                uriPropertiesTwo.setCounter(counter);
                uriMap.put(uri2, uriPropertiesTwo);
                sendEmail("the server with port 8080 down", "the server is down");
                LOG.info("Mail Sent for second uri");

            } else {
                int phaseCounter = uriMap.get(uri2).getPhaseCounter();
                phaseCounter++;
                uriPropertiesTwo.setPhaseCounter(phaseCounter);
                uriMap.put(uri2, uriPropertiesTwo);
            }


        }
    }


    @Scheduled(fixedRate = 120000)
    public void SchedulingMailingEventThree() {
        LOG.info("Scheduling EventThree");

        ResponseEntity<String> outThree;

        try {
            outThree = checkResponseStatus(uri3);
            String statusCodeThree = String.valueOf(outThree.getStatusCode());
            Gson g = new Gson();
            Data bodyThree = g.fromJson(outThree.getBody(), Data.class);
            if (!(statusCodeThree.equals(SUCCESS_CODE)) || !(bodyThree.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of localhost with port 9090  down");
                sendEmail("localhost:9090 down", "server instance of localhost with port 9090  down");
                LOG.info("Mail Sent for third uri");
            }
            if ((uriMap.get(uri3).getCounter()) < COUNTER_LIMIT) {
                LOG.info("Server is Up with port 9090");
                uriPropertiesThree.setCounter(3);
                uriPropertiesThree.setPhaseCounter(1);
                uriPropertiesThree.setMaxCounter(1);
                uriMap.put(uri3, uriPropertiesThree);
                sendEmail("localhost:9090 is up","server instance of localhost with port 9090 up ");
                LOG.info("Mail Sent for third uri");
            }
        } catch (Exception e) {

            LOG.error("server instance of localhost with port 9090  down");

            if ((uriMap.get(uri3).getPhaseCounter()) == (uriMap.get(uri3).getMaxCounter())) {
                int counter = uriMap.get(uri3).getCounter();
                if (counter == 0) {
                    int currentMaxCounter = (uriPropertiesThree.getMaxCounter()) * 2; //increaing counter by factor of 2
                    uriPropertiesThree.setMaxCounter(currentMaxCounter);
                    uriPropertiesThree.setPhaseCounter(1);
                    uriPropertiesThree.setCounter(3);
                    uriMap.put(uri3, uriPropertiesThree);
                }
                counter = uriPropertiesThree.getCounter();
                counter--;
                uriPropertiesThree.setCounter(counter);
                uriMap.put(uri3, uriPropertiesThree);
                sendEmail("the server with port 9090 down", "the server is down");
                LOG.info("Mail Sent for third uri");

            } else {
                int phaseCounter = uriMap.get(uri3).getPhaseCounter();
                phaseCounter++;
                uriPropertiesThree.setPhaseCounter(phaseCounter);
                uriMap.put(uri3, uriPropertiesThree);
            }

        }
    }

    public ResponseEntity<String> checkResponseStatus(String url) {
        ResponseEntity<String> output;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        output = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return output;
    }


}
