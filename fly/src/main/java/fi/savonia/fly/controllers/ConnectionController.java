package fi.savonia.fly.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ConnectionController {

    @GetMapping("/ready/{clientIp}")
    public ResponseEntity<String> saveDeciveIp(@PathVariable String clientIp) {
        RadarState.setIp(clientIp);
        System.out.println("IP: " + clientIp);
        return ResponseEntity.ok("Ip saved");
    }
}
