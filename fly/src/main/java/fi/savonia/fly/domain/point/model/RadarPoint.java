package fi.savonia.fly.domain.point.model;

public class RadarPoint {
    private int angle;
    private double distance;

    public RadarPoint(Point point) {
        this.angle = point.getAngle();
        this.distance = point.getDistance();
    }

    public RadarPoint(int angle, double distance) {
        this.angle = angle;
        this.distance = distance;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
