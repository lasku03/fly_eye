package fi.savonia.fly.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fi.savonia.fly.domain.detection.model.RadarDetection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.perimeter.model.RadarPerimeter;
import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.domain.point.model.RadarPoint;
import fi.savonia.fly.services.PerimeterService;

@RestController
@RequestMapping("/perimeters")
public class PerimeterController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PerimeterService perimeterService;

    List<Point> perimeterPoints = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<RadarPerimeter>> getAllPerimeters() {
        List<RadarPerimeter> radarPerimeters = perimeterService.getRadarPerimeterList();
        if (radarPerimeters != null) {
            return ResponseEntity.ok(radarPerimeters);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<List<Point>> getPerimeterPoints(@PathVariable int id) {
        List<Point> points = perimeterService.findPointsByPerimeterId(id);
        RadarState.setCurrentPerimeter(perimeterService.findPerimeterByPerimeterId(id));
        if (points != null) {
            return ResponseEntity.ok(points);
        } else {
            return ResponseEntity.notFound().build(); // Manejo de error si no se encuentra el perímetro
        }
    }

    @GetMapping("/{id}/detections")
    public ResponseEntity<List<RadarDetection>> getPerimeterDetections(@PathVariable int id) {
        List<RadarDetection> radarDetections = perimeterService.findRadarDetectionsByPerimeterId(id);
        RadarState.setCurrentHistoryPerimeter(perimeterService.findPerimeterByPerimeterId(id));
        if (radarDetections != null) {
            return ResponseEntity.ok(radarDetections);
        } else {
            return ResponseEntity.notFound().build(); // Manejo de error si no se encuentra el perímetro
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePerimeter(@PathVariable int id) {
        perimeterService.deletePerimeter(id);
        return ResponseEntity.ok("Perimeter " + id + " deleted");
    }

    @GetMapping("/start/{name}")
    public ResponseEntity<String> startNewPerimeterScan(@PathVariable String name) {
        Perimeter perimeter = Perimeter.builder()
                .name(name)
                .build();
        Perimeter newPerimeter = perimeterService.savePerimeter(perimeter);
        perimeterPoints = new ArrayList<>();
        if (newPerimeter != null) {
            RadarState.setCurrentPerimeter(newPerimeter);
            RadarPerimeter lastRadarPerimeter = new RadarPerimeter(newPerimeter);
            messagingTemplate.convertAndSend("/topic/newPerimeter", lastRadarPerimeter);

            if (makeHTTPRequest("perimeters")) {
                return ResponseEntity.ok(null);
            }
            else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build(); // Manejo de error si no se encuentra el perímetro
        }
    }

    @PostMapping("/{angle}/{distance}/{direction}")
    public ResponseEntity<RadarPerimeter> addPerimeterPoint(@PathVariable int angle, @PathVariable double distance,
            @PathVariable String direction) {
        if (RadarState.getCurrentPerimeter() != null) {
            distance = distance >= RadarState.getScale() ? 0 : distance / RadarState.getScale();
            RadarState.setDirection(direction);
            if (angle != RadarState.getLastPerimeterAngle()) {
                int angleDifference = Math.abs(angle - RadarState.getLastPerimeterAngle());
                if (angleDifference > 1 && angleDifference <= RadarState.getMaximumAngleDistance()) {
                    double lastDistance = RadarState.getLastPerimeterDistance() == 0 ? 1 : RadarState.getLastPerimeterDistance();
                    double thisDistance = distance == 0 ? 1 : distance;
                    double distanceToAdd = (thisDistance - lastDistance) / angleDifference;

                    if (direction.equals("COUNTERCLOCKWISE")) {
                        for (int i = RadarState.getLastPerimeterAngle() + 1; i < angle; i++) {
                            lastDistance += distanceToAdd;
                            lastDistance = lastDistance == 1 ? 0 : lastDistance;
                            Point midPoint = Point.builder()
                                    .angle(i)
                                    .distance(lastDistance)
                                    .build();
                            addPointToCurrentPerimeter(midPoint);
                            perimeterPoints.add(midPoint);
                        }
                    } else {
                        for (int i = RadarState.getLastPerimeterAngle() - 1; i > angle; i--) {
                            lastDistance += distanceToAdd;
                            lastDistance = lastDistance == 1 ? 0 : lastDistance;
                            Point midPoint = Point.builder()
                                    .angle(i)
                                    .distance(lastDistance)
                                    .build();
                            addPointToCurrentPerimeter(midPoint);
                            perimeterPoints.add(midPoint);
                        }
                    }
                }

                Point point = Point.builder()
                        .angle(angle)
                        .distance(distance)
                        .build();
                
                perimeterPoints.add(point);

                RadarState.setLastPerimeterAngle(angle);
                RadarState.setLastPerimeterDistance(distance);
                addPointToCurrentPerimeter(point);

                if (angle == 359) {
                    new Thread(() -> {
                        try {
                            perimeterService.addPointsToCurrentPerimeter(perimeterPoints);
                            Thread.sleep(100); // Delay for 0.1 second
                            boolean success = makeHTTPRequest("dedections");
                            System.out.println("HTTP Request success: " + success);
                        } catch (InterruptedException e) {
                            System.out.println("Thread interrupted: " + e.getMessage());
                        }
                    }).start();
                }
                else {
                    new Thread(() -> {
                        perimeterService.addPointsToCurrentPerimeter(perimeterPoints);
                    }).start();
                }

                return ResponseEntity.ok(new RadarPerimeter(RadarState.getCurrentPerimeter()));
            }

            return ResponseEntity.ok(null);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean makeHTTPRequest(String type) {
        String radarIp = RadarState.getIp();

        try {
            // Make a GET request to the radar
            System.out.println("Sending request of " + type);
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + radarIp + ":8081/start/" + type;
            String response = restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void addPointToCurrentPerimeter(Point point) {
        Perimeter currentPerimeter = RadarState.getCurrentPerimeter();
        addIfNotExist(currentPerimeter, point);
        RadarPerimeter lastRadarPerimeter = new RadarPerimeter(currentPerimeter);
        messagingTemplate.convertAndSend("/topic/newPerimeterPoint", lastRadarPerimeter);
    }

    public void addIfNotExist(Perimeter perimeter, Point newPoint) {
        if (perimeter.getPoints() == null) {
            perimeter.setPoints(new ArrayList<>());
        }
        Point point = perimeter.getAnglePoint(newPoint.getAngle());
        if (point == null) {
            perimeter.getPoints().add(newPoint);
        }
    }

    @GetMapping("/end")
    public ResponseEntity<RadarPerimeter> endNewPerimeterScan() {
        RadarPerimeter lastRadarPerimeter = new RadarPerimeter(RadarState.getCurrentPerimeter());
        messagingTemplate.convertAndSend("/topic/newPerimeter", lastRadarPerimeter);
        return ResponseEntity.ok(lastRadarPerimeter);
    }
}
