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

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MonitoringURIScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringURIScheduler.class.getName());

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

    private Map<String,Integer> counterMap;

    private Map<String,Integer> counterTime;

    @PostConstruct
    public void createHashMap(){
        counterMap = new HashMap<String, Integer>() {{
            put(uri1, 3);
            put(uri2, 3);
            put(uri3, 3);
        }};

        counterTime = new HashMap<String, Integer>() {{
            put(uri1, 4000);
            put(uri2, 4000);
            put(uri3, 4000);
        }};
    }


    public void sendEmail(String subject, String content) {

        LOG.info("Sending Email");

        String[] emailRecipients = setToEmail.split(",");


        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(emailRecipients);
        msg.setSubject(subject);
        msg.setText(content);
        javaMailSender.send(msg);

    }

//    @Scheduled(fixedRate = 2000)
//    public void SchedulingMailingEventOne() {
//
//        LOG.info("Scheduling EventOne");
//
//        System.out.println("Scheduling Event One");
//
//        ResponseEntity<String> outOne;
//
//        try{
//            outOne = SchedulingMailingEvent1(uri1);
//            String statusCodeOne = String.valueOf(outOne.getStatusCode());
//            Gson g = new Gson();
//            Data bodyOne = g.fromJson(outOne.getBody(), Data.class);
//            if (!(statusCodeOne.equals(SUCCESS_CODE)) || !(bodyOne.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
//                LOG.error("server instance of kiwiplans down");
//                // sendEmail("KiwiPlans server down", "server instance of kiwiplans down");
//            }
//            if(counterMap.get(uri1)<3){
//                LOG.info("Server is Up staging server");
//                counterMap.put(uri1,3);
//                //sendEmail("staging server is up","server instance of staging is up ");
//            }
//        }
//        catch(Exception e){
//            LOG.error("server instance of kiwiplans down");
//            int counter = counterMap.get(uri1);
//            if(counter==0){
//                System.out.println("Increasing cpounter of staging server");
//                int timer = counterTime.get(uri1);
//                timer = timer*2;
//                counterTime.put(uri1,timer);
//                counterMap.put(uri1,3);
//            }
//
//            counter--;
//            counterMap.put(uri1,counter);
//
//            System.out.println("Counter of staging error "+counter);
//
//            if((counterTime.get(uri1))%5000 ==0){
//                //sendEmail("KiwiPlans server down", "server instance of kiwiplans down");
//            }
//
//
//
//        }
//
//    }


    @Scheduled(fixedRate = 2000)
    public void SchedulingMailingEventTwo(){
        LOG.info("Scheduling EventTwo");
        System.out.println("Scheduling Event Two");
        ResponseEntity<String> outTwo ;

        try{
            outTwo = SchedulingMailingEvent1(uri2);
            String statusCodeTwo = String.valueOf(outTwo.getStatusCode());
            Gson g = new Gson();
            Data bodyTwo = g.fromJson(outTwo.getBody(), Data.class);
            if (!(statusCodeTwo.equals(SUCCESS_CODE)) || !(bodyTwo.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error("server instance of localhost with port 8080  down");
                //sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");
            }

            if(counterMap.get(uri2)<3){
                LOG.info("Server is Up with port 8080");
                counterMap.put(uri2,3);
                //sendEmail("localhost:8080 is up","server instance of localhost with port 8080 up again ");
            }

        }
        catch(Exception e){

            LOG.error("server instance of localhost with port 8080  down");
            int counter = counterMap.get(uri2);
            System.out.println("Checking Counter"+counter);
            if(counter==0){
                System.out.println("Increasing cpounter of 8080");
                int timer = counterTime.get(uri2);
                timer = timer*2;
                counterTime.put(uri2,timer);
                counterMap.put(uri2,3);
                counter=counterMap.get(uri2);
            }
            counter--;
            counterMap.put(uri2,counter);

            System.out.println("Counter of 8080 "+counterMap.get(uri2));

            if((counterTime.get(uri2))%5000 ==0){
                System.out.println("Sending email now"+counterTime.get(uri2));
                //sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");
            }
            //sendEmail("localhost:8080 down", "server instance of localhost with port 8080  down");

        }


    }

//
//    @Scheduled(fixedRate = 2000)
//    public void SchedulingMailingEventThree(){
//        LOG.info("Scheduling EventThree");
//
//        System.out.println("Scheduling Event Three");
//        ResponseEntity<String> outThree;
//
//        try{
//            outThree = SchedulingMailingEvent1(uri3);
//            String statusCodeThree = String.valueOf(outThree.getStatusCode());
//            Gson g = new Gson();
//            Data bodyThree = g.fromJson(outThree.getBody(), Data.class);
//            if (!(statusCodeThree.equals(SUCCESS_CODE)) || !(bodyThree.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
//                LOG.error("server instance of localhost with port 9090  down");
//                //sendEmail("localhost:9090 down", "server instance of localhost with port 9090  down");
//            }
//            if(counterMap.get(uri3)<3){
//                LOG.info("Server is Up with port 9090");
//                counterMap.put(uri3,3);
//                //sendEmail("localhost:9090 is up","server instance of localhost with port 9090 up ");
//            }
//        }
//        catch(Exception e){
//
//            LOG.error("server instance of localhost with port 9090  down");
//
//            int counter = counterMap.get(uri3);
//            if(counter==0){
//                System.out.println("Increasing cpounter of 9090");
//                int timer = counterTime.get(uri3);
//                timer = timer*2;
//                counterTime.put(uri3,timer);
//                counterMap.put(uri3,3);
//            }
//            counter--;
//            counterMap.put(uri3,counter);
//            System.out.println("Counter of 9090 "+counter);
//
//            if((counterTime.get(uri3))%5000 ==0){
//                //sendEmail("localhost:9090 down", "server instance of localhost with port 9090  down");
//            }
//
//
//
//
//
//        }

//    }

    public ResponseEntity<String>  SchedulingMailingEvent1(String url){
        ResponseEntity<String> output;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        output = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return output;
    }


}
