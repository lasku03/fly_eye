package fi.savonia.fly.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.perimeter.model.RadarPerimeter;
import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.services.PerimeterService;

@RestController
@RequestMapping("/perimeters")
public class PerimeterController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PerimeterService perimeterService;

    @GetMapping
    public ResponseEntity<List<RadarPerimeter>> getAllPerimeters() {
        List<RadarPerimeter> radarPerimeters = perimeterService.getRadarPerimeterList();
        if (radarPerimeters != null) {
            return ResponseEntity.ok(radarPerimeters);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/save-scan")
    public ResponseEntity<Perimeter> savePerimeter(@RequestBody Perimeter perimeter) {
        Perimeter savedPerimeter = perimeterService.savePerimeter(perimeter);
        if (savedPerimeter != null) {
            return ResponseEntity.ok(savedPerimeter);
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

    @GetMapping("/start/{name}")
    public ResponseEntity<String> startNewPerimeterScan(@PathVariable String name) {
        Perimeter perimeter = Perimeter.builder()
                .name(name)
                .build();
        Perimeter newPerimeter = perimeterService.savePerimeter(perimeter);
        if (newPerimeter != null) {
            RadarState.setCurrentPerimeter(newPerimeter);
            RadarPerimeter lastRadarPerimeter = new RadarPerimeter(newPerimeter);
            messagingTemplate.convertAndSend("/topic/newPerimeter", lastRadarPerimeter);
            return ResponseEntity.ok("Perimeter created");
        } else {
            return ResponseEntity.notFound().build(); // Manejo de error si no se encuentra el perímetro
        }
    }

    @PostMapping("/{angle}/{distance}")
    public ResponseEntity<RadarPerimeter> addPerimeterPoint(@PathVariable int angle, @PathVariable double distance) {
        if (RadarState.getCurrentPerimeter() != null) {
            List<Point> poinstToAdd = new ArrayList<>();

            if (RadarState.isDirection() && angle - RadarState.getLastPerimeterAngle() != 1) {
                for (int i = RadarState.getLastPerimeterAngle() + 1; i < angle; i++) {
                    Point midPoint = Point.builder()
                            .angle(i)
                            .distance(0)
                            .build();
                    poinstToAdd.add(midPoint);
                }
            } else if (!RadarState.isDirection() && RadarState.getLastPerimeterAngle() - angle != 1) {
                for (int i = RadarState.getLastPerimeterAngle() - 1; i > angle; i--) {
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

            poinstToAdd.add(point);
            RadarState.setLastPerimeterAngle(angle);
            perimeterService.addPointToCurrentPerimeter(poinstToAdd);

            RadarPerimeter lastRadarPerimeter = new RadarPerimeter(RadarState.getCurrentPerimeter());
            messagingTemplate.convertAndSend("/topic/newPerimeterPoint", lastRadarPerimeter);

            return ResponseEntity.ok(lastRadarPerimeter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/end")
    public ResponseEntity<RadarPerimeter> endNewPerimeterScan() {
        RadarPerimeter lastRadarPerimeter = new RadarPerimeter(RadarState.getCurrentPerimeter());
        messagingTemplate.convertAndSend("/topic/newPerimeter", lastRadarPerimeter);
        return ResponseEntity.ok(lastRadarPerimeter);
    }
}
