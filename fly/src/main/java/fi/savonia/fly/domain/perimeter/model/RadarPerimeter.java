package fi.savonia.fly.domain.perimeter.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.savonia.fly.domain.point.model.Point;
import fi.savonia.fly.domain.point.model.RadarPoint;

public class RadarPerimeter {
    private int perimeterID;
    private String name;
    private Date date;
    List<RadarPoint> points;

    public RadarPerimeter(Perimeter perimeter) {
        this.perimeterID = perimeter.getPerimeterID();
        this.name = perimeter.getName();
        this.date = perimeter.getDate();
        this.points = convertPointsToRadarPoints(perimeter.getPoints());
    }

    public RadarPerimeter(int perimeterID, String name, Date date, List<RadarPoint> points) {
        this.perimeterID = perimeterID;
        this.name = name;
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

    public int getPerimeterID() {
        return perimeterID;
    }

    public void setPerimeterID(int perimeterID) {
        this.perimeterID = perimeterID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
