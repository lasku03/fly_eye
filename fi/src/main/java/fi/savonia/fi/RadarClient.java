package fi.savonia.fi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@SpringBootApplication
public class RadarClient {

    private static final String BASE_URL = "http://localhost:8080/detections"; // Cambia la URL si es necesario
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Random random = new Random();

    public static void main(String[] args) {
        SpringApplication.run(RadarClient.class, args);
        simulateRadarDetection();
    }

    private static void simulateRadarDetection() {
        for (int angle = 0; angle < 360; angle++) {
            int randomNumber = random.nextInt(10) + 1; // Número aleatorio entre 1 y 10
            double distance;

            if (randomNumber == 10) {
                distance = 0.05 + (0.99 - 0.05) * random.nextDouble(); // Distancia aleatoria entre 0.01 y 0.99
                distance = Math.round(distance * 100.0) / 100.0;
            } else {
                distance = 0; // No se detectó nada
            }

            ResponseEntity<String> response = addDetectionPoint(angle, distance);
            System.out.println("Angle: " + angle + ", Random Number: " + randomNumber + ", Distance: " + distance + ", Response: " + response.getBody());
        }
    }

    private static ResponseEntity<String> addDetectionPoint(int angle, double distance) {
        String url = BASE_URL + "/" + angle + "/" + distance;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
}
