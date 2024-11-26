#include <Arduino.h>
#include "motorDriver.h"

static int dutyCicle = 0;
static bool motorIsRunning = false;  // Drives the motor with manipulated speed if true, the logic sets and resets only this variable in order to produce the pwm signal. The pwm is generated in the ISR.

void motdriv_Init() {
  // Pin configurations
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(PWM_PIN, OUTPUT);

  // Set all pins to low
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);
  digitalWrite(PWM_PIN, LOW);

  // Configurate timer for speed manipulation
  // Disable interrupts during timer setup
  cli();

  // Configure Timer 1
  TCCR1A = 0;               // Normal operation (no PWM)
  TCCR1B = 0;               // Clear the control register
  TCNT1 = 0;                // Reset Timer 1 count
  OCR1A = 999;              // Compare match value for 500 µs
  TCCR1B |= (1 << WGM12);   // CTC mode (Clear Timer on Compare Match)
  TCCR1B |= (1 << CS11);    // Prescaler: 8
  TIMSK1 |= (1 << OCIE1A);  // Enable Timer Compare Interrupt

  // Re-enable interrupts
  sei();
}

// ISR to manipulate speed when timer expired
ISR(TIMER1_COMPA_vect) {
  // Drive motor
  static bool state;
  static uint8_t cycleCounter;
  const uint8_t noOfCyclesPwm = 10;
  const uint8_t noOfCyclesOff = 60;
  if (motorIsRunning) {
    // PWM
    if (cycleCounter++ < noOfCyclesPwm) {
      // state = !state;
      state = true;
    }
    // OFF
    else {
      state = false;
    }
    // Reset cycle counter
    if (cycleCounter >= noOfCyclesPwm + noOfCyclesOff) {
      cycleCounter = 0;
    }

  } else {
    state = false;
  }
  digitalWrite(PWM_PIN, state);
}

void motdriv_Drive(uint8_t speed, enDirection direction) {
  // The speed is actually not used because the speed control needed to be implemented through timers!!!

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
  dutyCicle = int(speed) * 255 / 100;

  // Drive motor
  motorIsRunning = true;
}

void motdriv_Stop() {
  // Stop motor
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);

  // Stop pwm
  motorIsRunning = false;
}