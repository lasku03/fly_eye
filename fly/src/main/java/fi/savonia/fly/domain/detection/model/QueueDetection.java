package fi.savonia.fly.domain.detection.model;

import java.util.List;

import fi.savonia.fly.domain.point.model.Point;

public class QueueDetection {
    private Detection detection;
    private List<Point> points;

    public QueueDetection(Detection detection, List<Point> points) {
        this.detection = detection;
        this.points = points;
    }
    
    public Detection getDetection() {
        return detection;
    }
    public void setDetection(Detection detection) {
        this.detection = detection;
    }
    public List<Point> getPoints() {
        return points;
    }
    public void setPoints(List<Point> points) {
        this.points = points;
    }

    
}
