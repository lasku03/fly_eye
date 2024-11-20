#include "motorDriver.h"

void setup() {
  Serial.begin(115200);

  // test_serialCom();
  

  // Initializations
  // sercom_Init();
  motdriv_Init();

  Serial.println("------Initialization done-------");
  
}

void loop() {
  // Hyperloop
  


}
