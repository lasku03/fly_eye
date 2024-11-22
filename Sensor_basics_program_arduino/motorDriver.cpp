#include <Arduino.h>
#include "motorDriver.h"

void motdriv_Init() {
  // Pin configurations
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(PWM_PIN, OUTPUT);

  // Set all pins to low
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);
  digitalWrite(PWM_PIN, LOW);
}

void motdriv_Drive(uint8_t speed, enDirection direction) {
  // Set direction
  switch (direction) {
    case CLOCK_WISE:
      digitalWrite(IN1_PIN, LOW);
      digitalWrite(IN2_PIN, HIGH);
      break;
    case COUNTER_CLOCK_WISE:
      digitalWrite(IN1_PIN, HIGH);
      digitalWrite(IN2_PIN, LOW);
      break;
    default:
      break;
  }

  // Set PWM dutycycle
  int dutyCicle = int(speed) * 255 / 100;
  analogWrite(PWM_PIN, dutyCicle);

}

void motdriv_Stop() {
  // Stop motor
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);

  // Set PWM dutycycle to 0
  digitalWrite(PWM_PIN, LOW);
}