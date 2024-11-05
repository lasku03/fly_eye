package fi.savonia.fi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Random;

@SpringBootApplication
public class RadarPerimeter {

    private static final String BASE_URL = "http://localhost:8080/perimeters"; // Cambia la URL si es necesario
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Random random = new Random();

    public static void main(String[] args) {
        SpringApplication.run(RadarPerimeter.class, args);
        scanPerimeter();
    }

    public static void scanPerimeter() {
        // Generar un nombre aleatorio de 4 letras
        String perimeterName = generateRandomName(4);
        
        // Llamar a startNewPerimeterScan
        ResponseEntity<String> startResponse = startNewPerimeterScan(perimeterName);
        if (startResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Perimeter created: " + startResponse.getBody());

            // Generar y enviar distancias aleatorias para cada ángulo
            for (int angle = 0; angle < 360; angle++) {
                double randomDistance = 0.8; //generateRandomDistance();
                ResponseEntity<String> pointResponse = addPerimeterPoint(angle, randomDistance);
                if (pointResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Point added at angle " + angle + ": " + randomDistance);
                } else {
                    System.out.println("Failed to add point at angle " + angle);
                }
            }
        } else {
            System.out.println("Failed to create perimeter: " + startResponse.getStatusCode());
        }
    }

    private static ResponseEntity<String> startNewPerimeterScan(String name) {
        String url = BASE_URL + "/start/" + name;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    private static ResponseEntity<String> addPerimeterPoint(int angle, double distance) {
        String url = BASE_URL + "/" + angle + "/" + distance;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private static String generateRandomName(int length) {
        StringBuilder name = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char randomChar = (char) ('A' + random.nextInt(26)); // Generar letra aleatoria
            name.append(randomChar);
        }
        return name.toString();
    }

    private static double generateRandomDistance() {
        double distance = 0.8 + (1.1 - 0.8) * random.nextDouble(); // Generar valor entre 0.8 y 1.05
        distance = Math.round(distance * 100.0) / 100.0; // Redondear a 2 decimales
        return distance > 1 ? 0 : distance; // Si es mayor que 1, devolver 0
    }
    
}

