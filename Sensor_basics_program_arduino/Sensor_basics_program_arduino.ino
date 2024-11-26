#include "stateMachine.h"
#include "serialCommunication.h"
#include "distanceSensor.h"
#include "motor.h"

void setup() {
  Serial.begin(115200);  

  // Initializations
  sercom_Init();
  distsens_Init();
  mot_Init();

  Serial.println("------Initialization done-------");

  // Serial.println("Enter 'start' or 'stop'");
}

void loop() {
  // Hyperloop
  sm_DoWork();
  distsens_DoWork();
  mot_DoWork();
  




  // // Test motor from serial terminal
  // if(Serial.available() > 0) {
  //   String input = Serial.readStringUntil('\n');
  //   Serial.print("Entered string: ");
  //   Serial.println(input);
  //   if(input == "start") {
  //     Serial.println("Start motor");
  //     mot_Run();
  //   } else if(input == "stop") {
  //     Serial.println("Stop motor");
  //     mot_Stop();
  //   }
  // }
  


}
