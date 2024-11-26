#include <Arduino.h>
#include "motor.h"
#include "motorDriver.h"
#include "motorPosition.h"

#define motorSpeed 35   // Motorspeed in % (dutycycle)

static enMotorState state = MOT_STOP;

void mot_Init() {
  state = MOT_STOP;

  motpos_Init();
  motdriv_Init();
}

void mot_Run() {
  state = MOT_RUN;
}

void mot_Stop() {
  state = MOT_STOP;
}

void mot_DoWork() {
  // Execute ISR position sensors
  motpos_DoWork();

  // Drive motor
  static enDirection direction;
  static enDirection directionOld;
  unsigned long timeActual;
  static unsigned long timePrev;
  switch(state) {
    case MOT_STOP:
      motdriv_Stop();
      break;
    case MOT_RUN:
      direction = motpos_GetDirection();
      if(directionOld != direction) {
        state = MOT_BREAK;
        timePrev = millis();
        break;
      }
      motdriv_Drive(motorSpeed, direction);
      break;
    case MOT_BREAK:
      motdriv_Drive(motorSpeed / 2, directionOld);
      directionOld = direction;
      // Wait a moment (500ms)
      timeActual = millis();
      if(timeActual - timePrev >= 500) {
        state = MOT_WAIT;
        timePrev = timeActual;
      }
      break;
    case MOT_WAIT:
      motdriv_Stop();
      // Wait a moment (500ms)
      timeActual = millis();
      if(timeActual - timePrev >= 500) {
        state = MOT_RUN;
      }
      break;
    default:
      break;
  }
}