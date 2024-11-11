#include "motor.h"
#include "motorDriver.h"

#define motorSpeed 50   // Motorspeed in % (dutycycle)

static enMotorState state = STOP;

void mot_Init() {
  state = STOP;

  motdriv_Init();
}

void mot_Run() {
  state = RUN;
}

void mot_Stop() {
  state = STOP;
}

void mot_DoWork() {
  switch(state) {
    case STOP:
      motdriv_Stop();
      break;
    case RUN:
      enDirection direction = motpos_GetDirection();
      motdriv_Drive(motorSpeed, direction);
      break;
    default:
      break;
  }
}