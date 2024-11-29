package fi.savonia.fly.controllers;

import fi.savonia.fly.domain.detection.model.Detection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;

public class RadarState {
    private static String ip;
    private static int maximumAngleDistance = 10;
    private static String direction = "CLOCKWISE"; //"COUNTERCLOCKWISE"
    private static Perimeter currentPerimeter = null;
    private static Perimeter currentHistoryPerimeter = null;
    private static Detection currentDetection = null;
    private static int lastPerimeterAngle = -1;
    private static double lastPerimeterDistance = 0;
    private static int lastDetectionAngle = -1;
    private static double lastDetectionDistance = 0;
    private static int scale = 1000;

    public static int getScale() {
        return scale;
    }
    public static void setScale(int scale) {
        RadarState.scale = scale;
    }
    public static String getDirection() {
        return direction;
    }
    public static void setDirection(String direction) {
        RadarState.direction = direction;
    }
    public static Perimeter getCurrentPerimeter() {
        return currentPerimeter;
    }
    public static void setCurrentPerimeter(Perimeter currentPerimeter) {
        RadarState.currentPerimeter = currentPerimeter;
    }
    public static int getLastPerimeterAngle() {
        return lastPerimeterAngle;
    }
    public static void setLastPerimeterAngle(int lastPerimeterAngle) {
        RadarState.lastPerimeterAngle = lastPerimeterAngle;
    }
    public static int getLastDetectionAngle() {
        return lastDetectionAngle;
    }
    public static void setLastDetectionAngle(int lastDetectionAngle) {
        RadarState.lastDetectionAngle = lastDetectionAngle;
    }
    public static Detection getCurrentDetection() {
        return currentDetection;
    }
    public static void setCurrentDetection(Detection currentDetection) {
        RadarState.currentDetection = currentDetection;
    }
    public static Perimeter getCurrentHistoryPerimeter() {
        return currentHistoryPerimeter;
    }
    public static void setCurrentHistoryPerimeter(Perimeter currentHistoryPerimeter) {
        RadarState.currentHistoryPerimeter = currentHistoryPerimeter;
    }
    public static String getIp() {
        return ip;
    }
    public static void setIp(String ip) {
        RadarState.ip = ip;
    }
    public static int getMaximumAngleDistance() {
        return maximumAngleDistance;
    }
    public static void setMaximumAngleDistance(int maximumAngleDistance) {
        RadarState.maximumAngleDistance = maximumAngleDistance;
    }
    public static double getLastPerimeterDistance() {
        return lastPerimeterDistance;
    }
    public static void setLastPerimeterDistance(double lastPerimeterDistance) {
        RadarState.lastPerimeterDistance = lastPerimeterDistance;
    }
    public static double getLastDetectionDistance() {
        return lastDetectionDistance;
    }
    public static void setLastDetectionDistance(double lastDetectionDistance) {
        RadarState.lastDetectionDistance = lastDetectionDistance;
    }
    
}
