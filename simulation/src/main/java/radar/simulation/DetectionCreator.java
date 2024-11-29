package radar.simulation;

import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.MediaType;

public class DetectionCreator {
    private final String BASE_URL = "http://localhost:8080/detections"; // Cambia la URL si es necesario
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();
    public static boolean stop = false;

    public void executeDetections() {
        simulateInverseRadarDetection();
        simulateRadarDetection();
    }

    private void simulateRadarDetection() {
        for (int angle = 0; angle <= 360; angle += 10) {
            if (stop) break;
            int randomNumber = random.nextInt(10) + 1; // Número aleatorio entre 1 y 10
            double distance;

            if (angle == 360)
                angle = 359;
            if (randomNumber <= 10) {
                distance = 0.2 + (0.99 - 0.2) * random.nextDouble(); // Distancia aleatoria entre 0.2 y 0.99
                distance = Math.round(distance * 100.0) / 100.0;
            } else {
                distance = 0; // No se detectó nada
            }

            distance *= 1000;

            ResponseEntity<String> response = addDetectionPointCounterclockwise(angle, distance);
            System.out.println("Angle: " + angle + ", Random Number: " + randomNumber + ", Distance: " + distance
                    + ", Response: " + response.getBody());
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void simulateInverseRadarDetection() {
        for (int angle = 360; angle >= 0; angle -= 10) {
            if (stop) break;
            int randomNumber = random.nextInt(10) + 1; // Número aleatorio entre 1 y 10
            double distance;

            if (angle == 360)
                angle = 359;
            else if (angle == 0)
                angle = 1;
            if (randomNumber <= 10) {
                distance = 0.2 + (0.99 - 0.2) * random.nextDouble(); // Distancia aleatoria entre 0.2 y 0.99
                distance = Math.round(distance * 100.0) / 100.0;
            } else {
                distance = 0; // No se detectó nada
            }

            distance *= 1000;
            ResponseEntity<String> response = addDetectionPointClockwise(angle, distance);
            System.out.println("Angle: " + angle + ", Random Number: " + randomNumber + ", Distance: " + distance
                    + ", Response: " + response.getBody());
            if (angle == 359)
                angle = 360;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private ResponseEntity<String> addDetectionPointClockwise(int angle, double distance) {
        String url = BASE_URL + "/" + angle + "/" + distance + "/CLOCKWISE";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private ResponseEntity<String> addDetectionPointCounterclockwise(int angle, double distance) {
        String url = BASE_URL + "/" + angle + "/" + distance + "/COUNTERCLOCKWISE";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
}