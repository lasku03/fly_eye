#include <Arduino.h>
#include "stateMachine.h"
#include "serialCommunication.h"
#include "motor.h"
#include "motorPosition.h"
#include "distanceSensor.h"

enum enState {
  SM_INIT,  // Initialization
  SM_ZERO,
  SM_READY,
  SM_WAIT,
  SM_PERIM1,
  SM_PERIM2,
  SM_PERIMIO,
  SM_START_RUN,
  SM_RUN,
  SM_STOP
};

static bool zeroPositionFlag = false;

static enState state = SM_INIT;

void sm_SetZeroPositionFlag() {
  zeroPositionFlag = true;
}

void sm_DoMeasurement() {
  mot_Run();
  uint16_t actualPos = motpos_GetActual();
  enDirection direction = motpos_GetDirection();
  float distance = distsens_GetDistance();
  sercom_WriteData(actualPos, distance, direction);
}

void sm_DoWork() {
  int16_t actualPos = 0;
  enSerRxCmd cmd = -1;
  switch (state) {
    case SM_INIT:
      Serial.println("Wait for 'get ready'");
      sercom_WaitForCmd(GETREADY);
      Serial.println("Drive to zero position...");
      motpos_ChangeDirectionManually(CLOCK_WISE);
      mot_Run();
      state = SM_ZERO;
      break;
    case SM_ZERO:  // Drive to the zero position slowly
      if (zeroPositionFlag) {
        mot_Stop();
        state = SM_READY;
      }
      break;
    case SM_READY:  // Send 'ready' over serial communication
      sercom_WriteCmd(READY);
      motpos_ChangeDirectionManually(COUNTER_CLOCK_WISE);
      state = SM_WAIT;
      Serial.println("Wait for command ('start' or 'start perimeter scan')");
      break;
    case SM_WAIT:  // Wait for command ('start' or 'start perimeter scan')
      cmd = sercom_ReadCmdWithoutWaiting();
      if (cmd == START) {
        state = SM_START_RUN;
        Serial.println("Start measurement...");
        Serial.println("Wait for command 'stop' to terminate the measurement");
      } else if (cmd == SCANPERIM) {
        state = SM_PERIM1;
        Serial.println("Start perimeter scan...");
      }
      break;
    case SM_PERIM1:  // Do perimeter scan until the middle
      motpos_ChangeDirectionManually(COUNTER_CLOCK_WISE);
      sm_DoMeasurement();
      actualPos = motpos_GetActual();
      if (actualPos >= 150 && actualPos <= 210) {
        state = SM_PERIM2;
        Serial.println("Middle of perimeter scan reached");
      }
      break;
    case SM_PERIM2:  // Do perimeter scan until the end
      sm_DoMeasurement();
      if (zeroPositionFlag) {
        mot_Stop();
        state = SM_PERIMIO;
        Serial.println("Perimeter scan done");
      }
      break;
    case SM_PERIMIO:  // Perimeter scan done
      sercom_WriteCmd(PERIMIO);
      motpos_ChangeDirectionManually(CLOCK_WISE);
      state = SM_WAIT;
      Serial.println("Wait for command ('start' or 'start perimeter scan')");
      break;
    case SM_START_RUN:  // Start with doing the measurement
      sm_DoMeasurement();
      actualPos = motpos_GetActual();
      if (actualPos >= 30 && actualPos <= 330) {
        state = SM_RUN;
      }
    case SM_RUN:  // Do the measurement    
      sm_DoMeasurement();
      static int i;
      if (++i >= 10) {  // Check command only every 10 times, because the function has a big latency.
        i = 0;
        cmd = sercom_ReadCmdWithoutWaiting();
        if (cmd == STOP) {
          state = SM_STOP;
          Serial.println("Measurement stopped");
        }
      }
      break;
    case SM_STOP:  // Stop measurement
      mot_Stop();
      state = SM_WAIT;
      Serial.println("Wait for command ('start' or 'start perimeter scan')");
      break;
  }
  // Serial.println(state);

  // Reset zero position flag
  zeroPositionFlag = false;

  if(state == SM_RUN) {
    motpos_ChangeDirectionAutomaticallyEnable(true);
  }
  else {
    motpos_ChangeDirectionAutomaticallyEnable(false);
  }
}