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

    @PostMapping("/{angle}/{distance}")
    public ResponseEntity<String> addDetectionPoint(@PathVariable int angle, @PathVariable double distance) {
        if (RadarState.getCurrentPerimeter() != null) {
            List<Point> poinstToAdd = new ArrayList<>();

            if (RadarState.isDirection() && angle - RadarState.getLastDetectionAngle() != 1) {
                for (int i = RadarState.getLastDetectionAngle() + 1; i < angle; i++) {
                    Point midPoint = Point.builder()
                            .angle(i)
                            .distance(0)
                            .build();
                    poinstToAdd.add(midPoint);
                }
            } else if (!RadarState.isDirection() && RadarState.getLastDetectionAngle() - angle != 1) {
                for (int i = RadarState.getLastDetectionAngle() - 1; i > angle; i--) {
                    Point midPoint = Point.builder()
                            .angle(i)
                            .distance(0)
                            .build();
                    poinstToAdd.add(midPoint);
                }
            }

            Point point = Point.builder()
                    .angle(angle)
                    .distance(distance)
                    .build();
            
            Point perimeterPoint = RadarState.getCurrentPerimeter().getAnglePoint(point.getAngle());
            if (perimeterPoint != null && perimeterPoint.getDistance() > 0 && point.getDistance() > perimeterPoint.getDistance() - 0.02) {
                point.setDistance(0);
            }
            poinstToAdd.add(point);
            RadarPoint radarPoint = new RadarPoint(point);

            RadarState.setLastDetectionAngle(angle);
            detectionService.addPointToCurrentDetection(poinstToAdd);

            messagingTemplate.convertAndSend("/topic/points", radarPoint);

            return ResponseEntity.ok("Point added: Angle = " + angle + ", Distance = " + distance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
