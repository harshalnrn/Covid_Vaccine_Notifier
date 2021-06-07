package main.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

//@RestController
//@RequestMapping("/controller")
@Configuration
@EnableScheduling
public class Notification1 {

    @Autowired
    JavaMailSender emailSender;

    //@GetMapping("/getAvailability")

    @Scheduled(fixedRate = 60000)
    public void getAvailability() throws Exception {
        Map<String, String> availability = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        LocalDate date = LocalDate.now();
        System.out.println(date.toString());
        //yyyy-mm-dd
        //      dd-mm-yyyy

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String todayDate = dateTimeFormatter.format(date);
        System.out.println(todayDate);
        String getUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=266&date=" + todayDate;


        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getUrl, String.class);
        String json = responseEntity.getBody();
        Map<String, Object> map = mapper.readValue(json, Map.class);  // converting json string to Map object

        System.out.println(map);
        List<Map> list = (ArrayList) map.get("centers"); //list of all centers of that district

        for (Map map1 : list) {                             // details of each center and their session data for mentioned date +6
            List<Map> list1 = (List) map1.get("sessions");
            for (Map<String, Object> map3 : list1) {
                if ((Integer) (map3.get("min_age_limit")) == 18 && (Integer) map3.get("available_capacity_dose1") > 0) {
                    availability.put(map1.get("name").toString(), " slotDate: "+map3.get("date").toString() + "available : " + map3.get("available_capacity_dose1").toString());
                }
            }

        }


        // sending json string object
if(availability.size()>0) {
    sendSimpleMessage("harshalnrn@gmail.com", " 18+ Vaccine Availability status for Mysore", mapper.writeValueAsString(availability));
}
    }

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noReply@sample.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
