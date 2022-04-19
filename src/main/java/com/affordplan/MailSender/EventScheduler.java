package com.affordplan.MailSender;


import com.affordplan.Model.Data;
import com.google.gson.Gson;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Timer;


@Component
public class EventScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(EventScheduler.class.getName());

    private static final String SUCCESS_CODE = "200 OK";

    private static final String SUCCESS_MESSAGE = "OperationSuccess";

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

    private int timeToExecute = 3;

    private int SecondsToSend = 2000;



    public void sendEmail(String subject, String content) {

        LOG.info("Sending Email");

        String[] emailRecipients = setToEmail.split(",");


        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(emailRecipients);
        msg.setSubject(subject);
        msg.setText(content);
        javaMailSender.send(msg);

    }



    @Scheduled(fixedRate = 1000)
    public void SchedulingMailingEvent() {

        LOG.info("Scheduling Event");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //set my entity
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ResponseEntity<String> outOne;

        try{
             outOne = restTemplate.exchange(uri1, HttpMethod.GET, entity, String.class);
            String statusCodeOne = String.valueOf(outOne.getStatusCode());
            Gson g = new Gson();
            Data bodyOne = g.fromJson(outOne.getBody(), Data.class);
            if (!(statusCodeOne.equals(SUCCESS_CODE)) || !(bodyOne.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of kiwiplans down");
               // sendEmail("KiwiPlans server down", "server instance of kiwiplans down");
            }
        }
        catch(Exception e){
            System.out.println(timeToExecute);
            while(timeToExecute>=0){
                if(timeToExecute==0){
                    SecondsToSend = SecondsToSend*5;
                    timeToExecute=3;
                }
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("running after"+SecondsToSend);
                    }
                }, SecondsToSend);

            }

            LOG.error("server instance of kiwiplans down");
            //sendEmail("KiwiPlans server down", "server instance of kiwiplans down");
        }

        ResponseEntity<String> outTwo ;

        try{
            outTwo = restTemplate.exchange(uri2, HttpMethod.GET, entity, String.class);
            String statusCodeOne = String.valueOf(outTwo.getStatusCode());
            Gson g = new Gson();
            Data bodyOne = g.fromJson(outTwo.getBody(), Data.class);
            if (!(statusCodeOne.equals(SUCCESS_CODE)) || !(bodyOne.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of localhost with port 8080  down");
                //sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");
            }

        }
        catch(Exception e){
            System.out.println(timeToExecute);
            while(timeToExecute>=0){

                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("running after"+SecondsToSend);
                    }
                }, SecondsToSend);

                timeToExecute--;

            }
            LOG.error("server instance of localhost with port 8080  down");
            //sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");
        }
        ResponseEntity<String> outThree;

        try{
            outThree = restTemplate.exchange(uri3, HttpMethod.GET, entity, String.class);
            String statusCodeOne = String.valueOf(outThree.getStatusCode());
            Gson g = new Gson();
            Data bodyOne = g.fromJson(outThree.getBody(), Data.class);
            if (!(statusCodeOne.equals(SUCCESS_CODE)) || !(bodyOne.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of localhost with port 9090  down");
                //sendEmail("localhost:9090 down", "server instance of localhost with port 9090  down");
            }
        }
        catch(Exception e){
            LOG.error("server instance of localhost with port 9090  down");
            //sendEmail("localhost:9090 down", "server instance of localhost with port 9090  down");
        }

    }


}
