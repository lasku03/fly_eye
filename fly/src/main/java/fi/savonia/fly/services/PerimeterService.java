package fi.savonia.fly.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.savonia.fly.controllers.RadarState;
import fi.savonia.fly.domain.detection.model.Detection;
import fi.savonia.fly.domain.detection.model.RadarDetection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.perimeter.model.RadarPerimeter;
import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.repositories.PerimeterRepository;

@Service
public class PerimeterService {

    @Autowired
    private PerimeterRepository perimeterRepository;

    public Perimeter savePerimeter(Perimeter perimeter) {
        // TODO Auto-generated method stub
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        perimeter.setDate(date);

        // Persistir el objeto Perimeter en la base de datos
        return perimeterRepository.save(perimeter);
    }

    public List<RadarPerimeter> getRadarPerimeterList() {
        List<RadarPerimeter> radarPerimeters = new ArrayList<>();
        List<Perimeter> perimeters = perimeterRepository.findAll();
        for (Perimeter perimeter : perimeters) {
            radarPerimeters.add(new RadarPerimeter(perimeter));
        }
        return radarPerimeters;
    }

    public Perimeter findPerimeterByPerimeterId(int id) {
        Optional<Perimeter> perimeter = perimeterRepository.findById(id);
        return perimeter.get();
    }

    public List<Point> findPointsByPerimeterId(int id) {
        // TODO Auto-generated method stub
        Optional<Perimeter> perimeter = perimeterRepository.findById(id);
        List<Point> points = perimeter.get().getPoints();
        return points;
    }

    public List<RadarDetection> findRadarDetectionsByPerimeterId(int id) {
        // TODO Auto-generated method stub
        Optional<Perimeter> perimeter = perimeterRepository.findById(id);
        List<Detection> detections = perimeter.get().getDetections();
        List<RadarDetection> radarDetections = new ArrayList<>();
        for (Detection detection : detections) {
            radarDetections.add(new RadarDetection(detection));
        }
        return radarDetections;
    }

    public void createPerimeter() {
        List<Point> points = new ArrayList<>();
        Perimeter perimeter = Perimeter.builder()
                .points(points)
                .build();
        savePerimeter(perimeter);
    }

    public void addIfNotExist(Perimeter perimeter, Point newPoint) {
        Point point = perimeter.getAnglePoint(newPoint.getAngle());
        if (point == null) {
            perimeter.getPoints().add(newPoint);
        }
    }

    public void addPointsToCurrentPerimeter(List<Point> points) {
        // Obtain all perimeters
        Perimeter currentPerimeter = RadarState.getCurrentPerimeter();

        if (currentPerimeter.getPoints() == null) {
            currentPerimeter.setPoints(new ArrayList<>());
        }
        
        for (Point point : points) {
            addIfNotExist(currentPerimeter, point);
        }
        
        // Save the updated detection
        RadarState.setCurrentPerimeter(perimeterRepository.save(currentPerimeter));
    }

    public void saveCurrentPerimeter() {
        // Obtain all perimeters
        Perimeter currentPerimeter = RadarState.getCurrentPerimeter();
        RadarState.setCurrentPerimeter(perimeterRepository.save(currentPerimeter));
    }

    public void deletePerimeter(int id) {
        perimeterRepository.deleteById(id);
    }
}
