package fi.savonia.fly.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.savonia.fly.controllers.RadarState;
import fi.savonia.fly.domain.detection.model.Detection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.repositories.DetectionRepository;

@Service
public class DetectionService {

    @Autowired
    private DetectionRepository detectionRepository;

    @Autowired
    private PerimeterService perimeterService;

    public Detection saveDetection(Detection detection) {
        // TODO Auto-generated method stub
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        detection.setDate(date);
        
        Perimeter perimeter = perimeterService.findPerimeterByPerimeterId(RadarState.getCurrentPerimeter().getPerimeterID());
        detection.setPerimeter(perimeter);

        // Persistir el objeto Perimeter en la base de datos
        return detectionRepository.save(detection);
    }

    public void createDetection() {
        List<Point> points = new ArrayList<>();

        Detection detection = Detection.builder()
                .points(points)
                .direction(RadarState.getDirection())
                .build();
        Detection newDetection = saveDetection(detection);
        RadarState.setCurrentDetection(newDetection);
    }

    public void addIfNotExist(Detection detection, Point newPoint) {
        Point point = detection.getAnglePoint(newPoint.getAngle());
        if (point == null)  {
            detection.getPoints().add(newPoint);
        }
    }

    public void addPointsToCurrentDetection(List<Point> points) {
        Detection currentDetection = RadarState.getCurrentDetection();
        for (Point listPoint : points) {
            addIfNotExist(currentDetection, listPoint);
        }
        
        detectionRepository.save(currentDetection);
    }

    public void addPointsToDetection(Detection detection, List<Point> points) {
        for (Point listPoint : points) {
            addIfNotExist(detection, listPoint);
        }
        
        detectionRepository.save(detection);
    }

    public void addPointToCurrentDetection(Point point) {
        Detection currentDetection = RadarState.getCurrentDetection();
        
        addIfNotExist(currentDetection, point);
        // Save the updated detection
        detectionRepository.save(currentDetection);
    }
}
