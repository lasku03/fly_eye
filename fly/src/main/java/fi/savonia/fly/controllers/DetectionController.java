package fi.savonia.fly.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fi.savonia.fly.domain.detection.model.RadarDetection;
import fi.savonia.fly.domain.perimeter.model.RadarPerimeter;
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

    @PostMapping("/{angle}/{distance}/{direction}")
    public ResponseEntity<String> addDetectionPoint(@PathVariable int angle, @PathVariable double distance,
            @PathVariable String direction) {
        if (RadarState.getCurrentPerimeter() != null) {
            distance = distance >= RadarState.getScale() ? 0 : distance / RadarState.getScale();
            RadarState.setDirection(direction);
            List<Point> pointsToAdd = new ArrayList<>();
            if (angle != RadarState.getLastDetectionAngle()) {
                int thisAngle = angle;
                if (direction.equals("CLOCKWISE") && angle == 0) thisAngle = 360;
                int angleDifference = Math.abs(thisAngle - RadarState.getLastDetectionAngle());
                if (angleDifference > 1 && angleDifference <= RadarState.getMaximumAngleDistance()) {
                    double lastDistance = RadarState.getLastDetectionDistance() == 0 ? 1 : RadarState.getLastDetectionDistance();
                    double thisDistance = distance == 0 ? 1 : distance;
                    double distanceToAdd = (thisDistance - lastDistance) / angleDifference;

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
                    }
                    else {
                        int lastDetectionAngle = RadarState.getLastDetectionAngle() == 0 ? 360 : RadarState.getLastDetectionAngle();
                        for (int i = lastDetectionAngle - 1; i > angle; i--) {
                            lastDistance += distanceToAdd;
                            lastDistance = lastDistance == 1 ? 0 : lastDistance;
                            Point midPoint = Point.builder()
                                    .angle(i)
                                    .distance(isInsideThePerimeter(i, lastDistance) ? lastDistance : 0)
                                    .build();
                            addPointToCurrentDetection(midPoint);
                        }
                    }
                }

                Point point = Point.builder()
                        .angle(angle)
                        .distance(isInsideThePerimeter(angle, distance) ? distance : 0)
                        .build();

                addPointToCurrentDetection(point);
                pointsToAdd.add(point);

                new Thread(() -> {
                    detectionService.addPointsToCurrentDetection(pointsToAdd);
                }).start();

                RadarState.setLastDetectionAngle(angle);
                RadarState.setLastDetectionDistance(distance);
                

                return ResponseEntity.ok("Point added: Angle = " + angle + ", Distance = " + distance);
            }
            else {
                return ResponseEntity.ok(null);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    public boolean makeHTTPRequest() {
        String radarIp = RadarState.getIp();

        try {
            // Make a GET request to the radar
            System.out.println("Sending request of dedections");
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + radarIp + ":8081/start/dedections";
            String response = restTemplate.getForObject(url, String.class);
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
}
