package radar.simulation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Controller
public class RadarController {

    private static final String READY_URL = "http://localhost:8080/ready/127.0.0.1";
    private static final String BASE_URL = "http://localhost:8080/perimeters";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Random random = new Random();
    private static Thread radarThread;

    // Enviar "ready" cuando el servidor se inicia
    @Component
    public static class ReadySender implements CommandLineRunner {
        @Override
        public void run(String... args) {
            try {
                String response = restTemplate.getForObject(READY_URL, String.class);
                System.out.println("Sent 'ready'. Response: " + response);
            } catch (Exception e) {
                System.err.println("Failed to send 'ready': " + e.getMessage());
            }
        }
    }

    // Escucha el mensaje "start" y responde inmediatamente
    @GetMapping("/start/perimeters")
    public ResponseEntity<String> startRadar() {
        System.out.println("Received start signal. Launching radar perimeter...");

        // Crear y ejecutar un hilo manualmente
        Thread radarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                scanPerimeter();
            }
        });
        radarThread.start();

        return ResponseEntity.ok("Radar started");
    }

    @GetMapping("/start/dedections")
    public ResponseEntity<String> startDetections() {
        System.out.println("Received start signal. Launching radar detection...");
        DetectionCreator detectionCreator = new DetectionCreator();

        // Crear y ejecutar un hilo manualmente
        DetectionCreator.stop = false;

        radarThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!DetectionCreator.stop) {
                    detectionCreator.executeDetections();
                }
            }
        });
        radarThread.start();

        return ResponseEntity.ok("Detections started");
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopRadar() {
        System.out.println("Received stop signal. Stopping radar...");

        DetectionCreator.stop = true;
        if (radarThread != null) {
            try {
                radarThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        radarThread = null;
        System.out.println("Radar stopped");

        return ResponseEntity.ok("Radar stopped");
    }

    public static void scanPerimeter() {
        // Generar y enviar distancias aleatorias para cada ángulo
        for (int angle = 0; angle <= 360; angle += 10) {
            if (angle == 360)
                angle = 359;
            double randomDistance = generateRandomDistance();
            ResponseEntity<String> pointResponse = addPerimeterPoint(angle, randomDistance);
            if (pointResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("Point added at angle " + angle + ": " + randomDistance);
            } else {
                System.out.println("Failed to add point at angle " + angle);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static ResponseEntity<String> addPerimeterPoint(int angle, double distance) {
        String url = BASE_URL + "/" + angle + "/" + distance + "/COUNTERCLOCKWISE";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private static double generateRandomDistance() {
        double distance = 0.8 + (1.1 - 0.8) * random.nextDouble(); // Generar valor entre 0.8 y 1.05
        distance = Math.round(distance * 100.0) / 100.0; // Redondear a 2 decimales
        return distance > 1 ? 0 : distance * 1000; // Si es mayor que 1, devolver 0
    }
}
