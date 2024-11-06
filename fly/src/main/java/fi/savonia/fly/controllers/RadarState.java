package fi.savonia.fly.controllers;

import fi.savonia.fly.domain.detection.model.Detection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;

public class RadarState {
    
    private static boolean direction = true;
    private static Perimeter currentPerimeter = null;
    private static Perimeter currentHistoryPerimeter = null;
    private static Detection currentDetection = null;
    private static int lastPerimeterAngle = 0;
    private static int lastDetectionAngle = 0;

    public static boolean isDirection() {
        return direction;
    }
    public static void setDirection(boolean direction) {
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
}
