#include <Arduino.h>
#include "distanceSensor.h"

static float duration = 0.0;
static float distance = 0.0;  // In mm

void distsens_Init() {
  // Pin configurations
  pinMode(DISTANCE_TRIG_PIN, OUTPUT);
  pinMode(DISTANCE_ECHO_PIN, INPUT);

  // Set all pins to low
  digitalWrite(DISTANCE_TRIG_PIN, LOW);
}

float distsens_GetDistance() {
  return distance;
}

float distsens_TimeOfFlightToDistance(float timeOfFlight) {
  float c = .343;  // In mm/s
  float d = c * timeOfFlight / 2;
  return d;
}

void distsens_DoWork() {
  digitalWrite(DISTANCE_TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(DISTANCE_TRIG_PIN, LOW);

  duration = pulseIn(DISTANCE_ECHO_PIN, HIGH);
  distance = distsens_TimeOfFlightToDistance(duration);
}