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
        detection.setPerimeter(perimeterService.findPerimeterByPerimeterId(RadarState.getCurrentPerimeter().getPerimeterID()));

        // Persistir el objeto Perimeter en la base de datos
        return detectionRepository.save(detection);
    }

    public void createDetection() {
        List<Point> points = new ArrayList<>();

        Detection detection = Detection.builder()
                .points(points)
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
        Point point = points.get(points.size() - 1);
        if (point.getAngle() == 0 || RadarState.getCurrentDetection() == null) {
            createDetection();
        }

        Detection currentDetection = RadarState.getCurrentDetection();
        for (Point listPoint : points) {
            addIfNotExist(currentDetection, listPoint);
        }
        
        // Save the updated detection
        RadarState.setCurrentDetection(detectionRepository.save(currentDetection));
    }
}
