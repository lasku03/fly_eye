#include "distanceSensor.h"
#include "motorPosition.h"

void setup() {
  Serial.begin(115200);

  // test_serialCom();

  

  // Initializations
  // sercom_Init();
  distsens_Init();
  motpos_Init();

  Serial.println("------Initialization done-------");

  test_motdriv();
  
}

void loop() {
  // Hyperloop
  // distsens_DoWork();
  // float distance = distsens_GetDistance();
  // Serial.print("Distance: ");
  // Serial.println(distance);
  
  motpos_DoWork();
  


}
