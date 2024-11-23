#include "distanceSensor.h"
#include "motor.h"

void setup() {
  Serial.begin(115200);

  // test_serialCom();

  

  // Initializations
  // sercom_Init();
  // distsens_Init();
  mot_Init();

  Serial.println("------Initialization done-------");

  Serial.println("Enter 'start' or 'stop'");
}

void loop() {
  // Hyperloop
  // distsens_DoWork();
  // float distance = distsens_GetDistance();
  // Serial.print("Distance: ");
  // Serial.println(distance);
  mot_DoWork();




  // Test motor from serial terminal
  if(Serial.available() > 0) {
    String input = Serial.readStringUntil('\n');
    Serial.print("Entered string: ");
    Serial.println(input);
    if(input == "start") {
      Serial.println("Start motor");
      mot_Run();
    } else if(input == "stop") {
      Serial.println("Stop motor");
      mot_Stop();
    }
  }
  


}
