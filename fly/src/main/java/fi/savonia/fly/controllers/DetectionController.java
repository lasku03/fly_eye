package fi.savonia.fly.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.domain.point.model.RadarPoint;
import fi.savonia.fly.services.DetectionService;

@RestController
@RequestMapping("/detections")
public class DetectionController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DetectionService detectionService;

    private final BlockingQueue<List<Point>> pointsQueue = new ArrayBlockingQueue<>(1000);
    private Thread processingThread;

    public DetectionController() {
        startThread();
    }

    public void startThread() {
        processingThread = new Thread(this::processPoints);
        processingThread.start();
    }

    @PostMapping("/{angle}/{distance}/{direction}")
    public ResponseEntity<String> addDetectionPoint(@PathVariable int angle, @PathVariable double distance,
            @PathVariable String direction) throws InterruptedException {
        if (RadarState.getCurrentPerimeter() != null) {
            distance = distance >= RadarState.getScale() ? 0 : distance / RadarState.getScale();
            distance = Math.round(distance * 100.0) / 100.0;
            List<Point> pointsToAdd = new ArrayList<>();
            if (direction != RadarState.getDirection()) {
                detectionService.createDetection();
            }
            RadarState.setDirection(direction);
            int angleDifference = Math.abs(angle - RadarState.getLastDetectionAngle());
            if (angleDifference > 1) {
                double lastDistance = 0;
                double distanceToAdd = 0;
                if (angleDifference <= RadarState.getMaximumAngleDistance()) {
                    lastDistance = RadarState.getLastDetectionDistance() == 0 ? 1
                            : RadarState.getLastDetectionDistance();
                    double thisDistance = distance == 0 ? 1 : distance;
                    distanceToAdd = (thisDistance - lastDistance) / angleDifference;
                }

                if (direction.equals("COUNTERCLOCKWISE")) {
                    for (int i = RadarState.getLastDetectionAngle() + 1; i < angle; i++) {
                        lastDistance += distanceToAdd;
                        lastDistance = lastDistance == 1 ? 0 : lastDistance;
                        Point midPoint = Point.builder()
                                .angle(i)
                                .distance(isInsideThePerimeter(i, lastDistance) ? lastDistance : 0)
                                .build();
                        addPointToCurrentDetection(midPoint);
                        pointsToAdd.add(midPoint);
                    }
                } else {
                    int lastDetectionAngle = RadarState.getLastDetectionAngle() == 0 ? 360
                            : RadarState.getLastDetectionAngle();
                    for (int i = lastDetectionAngle - 1; i > angle; i--) {
                        lastDistance += distanceToAdd;
                        lastDistance = lastDistance == 1 ? 0 : lastDistance;
                        Point midPoint = Point.builder()
                                .angle(i)
                                .distance(isInsideThePerimeter(i, lastDistance) ? lastDistance : 0)
                                .build();
                        addPointToCurrentDetection(midPoint);
                        pointsToAdd.add(midPoint);
                    }
                }
            }

            Point point = Point.builder()
                    .angle(angle)
                    .distance(isInsideThePerimeter(angle, distance) ? distance : 0)
                    .build();

            addPointToCurrentDetection(point);
            pointsToAdd.add(point);
            pointsQueue.put(pointsToAdd);

            RadarState.setLastDetectionAngle(angle);
            RadarState.setLastDetectionDistance(distance);

            return ResponseEntity.ok("Point added: Angle = " + angle + ", Distance = " + distance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/start")
    public ResponseEntity<String> startDetections() {
        makeHTTPStartRequest();
        return ResponseEntity.ok(null);
    }

    public boolean makeHTTPStartRequest() {
        String radarIp = RadarState.getIp();

        try {
            // Make a GET request to the radar
            System.out.println("Sending request of dedections");
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + radarIp + ":8081/start/dedections";
            String response = restTemplate.getForObject(url, String.class);
            System.out.println(response);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean isInsideThePerimeter(int angle, double distance) {
        Point perimeterPoint = RadarState.getCurrentPerimeter().getAnglePoint(angle);
        double perimeterDistance = perimeterPoint.getDistance() == 0 ? 1 : perimeterPoint.getDistance();
        return perimeterPoint != null && distance > 0 && distance < perimeterDistance - 0.02;
    }

    public void addPointToCurrentDetection(Point point) {
        RadarPoint radarPoint = new RadarPoint(point);
        messagingTemplate.convertAndSend("/topic/points", radarPoint);
    }

    private void processPoints() {
        try {
            while (true) {
                List<Point> points = pointsQueue.take();
                for (Point point : points) {
                    detectionService.addPointToCurrentDetection(point);
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Interrumpe el hilo si ocurre un error
        }
    }
}
