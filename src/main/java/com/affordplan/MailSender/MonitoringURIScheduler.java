package com.affordplan.MailSender;

import com.affordplan.Model.Data;
import com.affordplan.Model.UriProperties;
import com.google.gson.Gson;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@PropertySource("classpath:application.properties")
public class MonitoringURIScheduler {

    public final static int COUNTER_LIMIT = 3;
    private static final Logger LOG = LogManager.getLogger(MonitoringURIScheduler.class);
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

        LOG.debug("Inside Send Email event",Instant.now().toString(),"inside send mail","");
        try {
            String[] emailRecipients = setToEmail.split(",");
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailRecipients);
            msg.setSubject(subject);
            msg.setText(content);
            javaMailSender.send(msg);
        } catch (Exception e) {
            LOG.debug("sendEmail error",java.time.LocalDateTime.now(),"","","",e.getMessage().toString());
        }



    }

    public ResponseEntity<String> checkResponseStatus(String url) {
        LOG.debug("Inside checkResponseStatus with url ",Instant.now().toString(),url);
        ResponseEntity<String> output;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        long time = System.currentTimeMillis();
        output = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        long responseTime = (System.currentTimeMillis() - time);

        if(responseTime>threshHold){

            LOG.info("",java.time.LocalDateTime.now(),url,output.getStatusCodeValue(),responseTime);

            sendEmail("server " + url + "slow response time", "server instance of " + url + " taking long response time");

        }

        LOG.info("",java.time.LocalDateTime.now(),url,output.getStatusCodeValue(),responseTime,"up",output.getBody());



        return output;
    }

    public void SchedulingMailingEvent(UriProperties uriproperties) {
        LOG.debug("Inside SchedulingMailingEvent for uri ",java.time.LocalDateTime.now(),"Inside SchedulingMailingEvent for uri",uriproperties.getUri());

        ResponseEntity<String> output = null;
        String statusCode = null;
        int count;
        try {
            output = checkResponseStatus(uriproperties.getUri());
            statusCode = String.valueOf(output.getStatusCode());
            Gson g = new Gson();
            Data outputBody = g.fromJson(output.getBody(), Data.class);

            if (!(statusCode.equals(SUCCESS_CODE)) && !(outputBody.getData().getSuccess().equals(SUCCESS_MESSAGE))) {
                LOG.error(uriproperties.getUri() ,java.time.LocalDateTime.now(),uriproperties.getUri(),"","","server instance of server " + uriproperties.getUri() + "down");
                sendEmail("server " + uriproperties.getUri() + " down", "server instance of " + uriproperties.getUri() + " down");
                LOG.debug("Mail sent for uri " , java.time.LocalDateTime.now(),"Mail sent for uri" ,uriproperties.getUri());
            }
            else{
                if(uriproperties.getCounter()>0){
                   LOG.info("Server is Up for server ",java.time.LocalDateTime.now(),uriproperties.getUri(),"","","Server is Up for server" + uriproperties.getUri());
                    sendEmail("server " + uriproperties.getUri() + " is up", "server instance of " + uriproperties.getUri() + " is up");
                    LOG.info("Mail sent for server up for uri",java.time.LocalDateTime.now(),"","Mail sent for server up for uri" + uriproperties.getUri());
                    uriproperties.setCounter(0);
                }
            }


        } catch ( HttpStatusCodeException e) {
            LOG.debug("Inside catch of SchedulingMailingEvent",Instant.now().toString(),"Inside catch of SchedulingMailingEvent Http Exception");
            LOG.error("",java.time.LocalDateTime.now(),uriproperties.getUri(),e.getStatusCode(),"","down");
             count = uriproperties.getCounter();
             count++;
            uriproperties.setCounter(count);
            if(uriproperties.getCounter()>0 && ((uriproperties.getCounter()<3 ) || (uriproperties.getCounter()%5==0))){
               LOG.debug("Mail Sent condition satisfied ",java.time.LocalDateTime.now(),"Mail Sent condition satisfied");
                sendEmail("server " + uriproperties.getUri() + " down", "server instance of " + uriproperties.getUri() + " down");
            }

        }catch(Exception e){
            LOG.debug("Inside catch of SchedulingMailingEvent",Instant.now().toString(),"Inside catch of SchedulingMailingEvent Exception");
            LOG.error("",java.time.LocalDateTime.now(),uriproperties.getUri(),"I/o Exception","down");
            count = uriproperties.getCounter();
            count++;
            uriproperties.setCounter(count);
            if(uriproperties.getCounter()>0 && ((uriproperties.getCounter()<3 ) || (uriproperties.getCounter()%5==0))){
                LOG.debug("Mail Sent condition satisfied ",java.time.LocalDateTime.now(),"Mail Sent condition satisfied");
                sendEmail("server " + uriproperties.getUri() + " down", "server instance of " + uriproperties.getUri() + " down");
            }
        }

    }

    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventOne() {
        LOG.debug("Inside SchedulingMailingEventOne",java.time.LocalDateTime.now(),"Inside SchedulingMailingEventOne");
        SchedulingMailingEvent(uriPropertiesOne);

    }

    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventTwo() {
        LOG.debug("Inside SchedulingMailingEventTwo",java.time.LocalDateTime.now(),"Inside SchedulingMailingEventTwo");

        SchedulingMailingEvent(uriPropertiesTwo);
    }


    @Scheduled(fixedRateString = "${Timer.time}")
    public void SchedulingMailingEventThree() {
        LOG.debug("Inside SchedulingEventThree",java.time.LocalDateTime.now(),"Inside SchedulingEventThree");

        SchedulingMailingEvent(uriPropertiesThree);

    }






}
