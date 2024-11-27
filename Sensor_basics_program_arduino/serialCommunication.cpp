#include <Arduino.h>
#include "serialCommunication.h"
#include "motorDriver.h"

void sercom_Init() {
  Serial.begin(SERIAL_BAUD_RATE);
}

void sercom_WriteData(uint16_t angle, float distance, enDirection direction) {
  Serial.print(angle);
  Serial.print(";");
  Serial.print(distance);
  Serial.print(";");

  if (direction == CLOCK_WISE) {
    Serial.println("CLOCKWISE");
  } else if (direction == COUNTER_CLOCK_WISE) {
    Serial.println("COUNTERCLOCKWISE");
  }

  Serial.flush();
}

void sercom_WriteCmd(enSerTxCmd cmd) {
  switch (cmd) {
    case READY:
      Serial.println("ready");
      break;
    case PERIMIO:
      Serial.println("perimeter scan done");
      break;
    default:
      break;
  }

  Serial.flush();
}

enSerRxCmd sercom_StringToCmd(String input) {
  if (input == "start") {
    return START;
  } else if (input == "stop") {
    return STOP;
  } else if (input == "get ready") {
    return GETREADY;
  } else if (input == "start perimeter scan") {
    return SCANPERIM;
  } else {
    return;
  }
}

enSerRxCmd sercom_ReadCmdWait() {
  String input = "";

  while (true) {
    if (Serial.available() > 0) {
      char received = Serial.read();  // Read the next character

      // Serial.print("Char read from serial buffer: ");
      // Serial.println(received);

      if (received == '\r') {  // Check if return character is entered
        break;
      } else if (received == '\n') {  // Check if newline character is entered
        continue;
      } else {
        input += received;
      }
    }
  }

  Serial.print("Whole received string: ");
  Serial.println(input);

  enSerRxCmd cmd = sercom_StringToCmd(input);

  return cmd;
}

enSerRxCmd sercom_ReadCmdWithoutWaiting() {
  if (Serial.available()) {
    String input = Serial.readStringUntil('\n');
    input.trim();
    // Serial.print("String received: ");
    // Serial.println(input);
    enSerRxCmd cmd = sercom_StringToCmd(input);
    // Serial.print("Cmd received: ");
    // Serial.println(cmd);

    if (cmd >= START && cmd <= SCANPERIM) {
      return cmd;
    }
  }
  return;
}

void sercom_WaitForCmd(enSerRxCmd cmdExp) {
  enSerRxCmd cmd = -1;

  while (true) {
    cmd = sercom_ReadCmdWait();
    // Serial.print("Received cmd: ");
    // Serial.println(cmd);
    if (cmd == cmdExp) {
      break;
    }
  }
}
