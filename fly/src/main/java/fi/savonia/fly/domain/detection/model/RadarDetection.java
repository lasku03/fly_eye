package fi.savonia.fly.domain.detection.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.domain.point.model.RadarPoint;

public class RadarDetection {
    private int detectionID;
    private Date date;
    List<RadarPoint> points;

    public RadarDetection(Detection detection) {
        this.detectionID = detection.getDetectionID();
        this.date = detection.getDate();
        this.points = convertPointsToRadarPoints(detection.getPoints());
    }

    public RadarDetection(int detectionID, Date date, List<RadarPoint> points) {
        this.detectionID = detectionID;
        this.date = date;
        this.points = points;
    }

    private List<RadarPoint> convertPointsToRadarPoints(List<Point> points) {
        List<RadarPoint> radarPoints = new ArrayList<>();
        if (points != null) {
            for (Point point : points) {
                radarPoints.add(new RadarPoint(point));
            }
        }
        return radarPoints;
    }

    public int getDetectionID() {
        return detectionID;
    }
    public void setDetectionID(int detectionID) {
        this.detectionID = detectionID;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public List<RadarPoint> getPoints() {
        return points;
    }
    public void setPoints(List<RadarPoint> points) {
        this.points = points;
    }
}