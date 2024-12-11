package fi.savonia.fly.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Controller
public class ConnectionController {

    @GetMapping("/ready/{clientIp}")
    public ResponseEntity<String> saveDeciveIp(@PathVariable String clientIp) {
        RadarState.setIp(clientIp);
        System.out.println("IP: " + clientIp);
        return ResponseEntity.ok("Ip saved");
    }

    @GetMapping("/stopRadar")
    public ResponseEntity<String> stopRadar() {
        if (makeHTTPStopRequest()) {
            return ResponseEntity.ok("Radar stopped");
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean makeHTTPStopRequest() {
        String radarIp = RadarState.getIp();

        try {
            // Make a GET request to the radar
            System.out.println("Sending request of stop");
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + radarIp + ":8081/stop";
            String response = restTemplate.getForObject(url, String.class);
            System.out.println(response);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
