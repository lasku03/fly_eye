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
  SM_RUN,
  SM_STOP
};

static enState state = SM_INIT;

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
      mot_Run();
      state = SM_ZERO;
      break;
    case SM_ZERO:  // Drive to the zero position slowly
      if (digitalRead(POSITION_REF_PIN)) {
        mot_Stop();
        state = SM_READY;
      }
      break;
    case SM_READY:  // Send 'ready' over serial communication
      motpos_ChangeDirectionManually(CLOCK_WISE);
      sercom_WriteCmd(READY);
      state = SM_WAIT;
      Serial.println("Wait for command ('start' or 'start perimeter scan')");
      break;
    case SM_WAIT:  // Wait for command ('start' or 'start perimeter scan')
      cmd = sercom_ReadCmdWithoutWaiting();
      if (cmd == START) {
        state = SM_RUN;
        Serial.println("Start measurement...");
        Serial.println("Wait for command 'stop' to terminate the measurement");
      } else if (cmd == SCANPERIM) {
        state = SM_PERIM1;
        Serial.println("Start perimeter scan...");
      }
      break;
    case SM_PERIM1:  // Do perimeter scan until the middle
      sm_DoMeasurement();
      actualPos = motpos_GetActual();
      if (actualPos >= 150 && actualPos <= 210) {
        state = SM_PERIM2;
        Serial.println("Middle of perimeter scan reached");
      }
      break;
    case SM_PERIM2:  // Do perimeter scan until the end
      sm_DoMeasurement();
      if (digitalRead(POSITION_REF_PIN)) {
        mot_Stop();
        state = SM_PERIMIO;
        Serial.println("Perimeter scan done");
      }
      break;
    case SM_PERIMIO:  // Perimeter scan done
      sercom_WriteCmd(PERIMIO);
      state = SM_WAIT;
      Serial.println("Wait for command ('start' or 'start perimeter scan')");
      break;
    case SM_RUN:  // Do the measurement
      sm_DoMeasurement();
      static int i;
      if (++i >= 10) {  // Check command only every 10 times, because the function as a big latency.
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
}