#include "Arduino.h"
#include "motorDriver.h"

void motdriv_Init() {
  // Pin configurations
  pinMode(ENABLE_PIN, OUTPUT);
  pinMode(PWM_PIN, OUTPUT);
  pinMode(DIRECTION_PIN, OUTPUT);

  // Set all pins to low
  digitalWrite(ENABLE_PIN, LOW);
  digitalWrite(PWM_PIN, LOW);
  digitalWrite(DIRECTION_PIN, LOW);
}

void motdriv_Drive(uint8_t speed, enDirection direction) {
  // Set direction
  switch (direction) {
    case CLOCK_WISE:
      digitalWrite(DIRECTION_PIN, LOW);
      break;
    case COUNTER_CLOCK_WISE:
      digitalWrite(DIRECTION_PIN, HIGH);
      break;
    default:
      break;
  }

  // Set PWM dutycycle
  int dutyCicle = int(speed) * 255 / 100;
  analogWrite(PWM_PIN, dutyCicle);

  // Start motor
  digitalWrite(ENABLE_PIN, HIGH);
}

void motdriv_Stop() {
  // Stop motor
  digitalWrite(ENABLE_PIN, LOW);

  // Set PWM dutycycle to 0
  digitalWrite(PWM_PIN, LOW);
}