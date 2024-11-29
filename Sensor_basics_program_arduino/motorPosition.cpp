#include <Arduino.h>
#include "motorPosition.h"
#include "stateMachine.h"

static int16_t actualPosition = 0;
static enDirection direction = CLOCK_WISE;

static bool isrPositionTriggered = false;
static bool isrZeroTriggered = false;

static const unsigned int noOfTurnsPerDirection = 1;
static unsigned int noOfTurns = 0;
static bool automaticDirectionChangeAllowed = false;
static bool automaticDirectionChangeToClockwiseAllowed = false;
static bool automaticDirectionChangeToCounterclockwiseAllowed = false;

static const unsigned int noOfPositionPoints = 18;  // The effective resolution is twice the number of position points, because the ISR is triggered with rising and falling edge.
static const unsigned int degreesPerPoint = 360 / noOfPositionPoints / 2;

static void motpos_IsrPosition() {
  // Serial.println("ISR Position fired");
  isrPositionTriggered = true;
}

static void motpos_IsrZero() {
  isrZeroTriggered = true;
  sm_SetZeroPositionFlag();
}

void motpos_Init() {
  // Pin configurations
  pinMode(POSITION_PIN, INPUT);
  pinMode(POSITION_REF_PIN, INPUT);

  // Attach interrupts to input pins
  attachInterrupt(digitalPinToInterrupt(POSITION_PIN), motpos_IsrPosition, CHANGE);
  attachInterrupt(digitalPinToInterrupt(POSITION_REF_PIN), motpos_IsrZero, RISING);
}

uint16_t motpos_GetActual() {
  int16_t pos = actualPosition;
  if (pos >= 0) {
    return (uint16_t)pos;
  } else {
    return (uint16_t)(pos + 360);
  }
}

enDirection motpos_GetDirection() {
  return direction;
}

void motpos_SetToZero() {
  actualPosition = 0;
}

void motpos_ChangeDirectionAutomaticallyEnable(bool en) {
  automaticDirectionChangeAllowed = en;
}

void motpos_ChangeDirectionManually(enDirection directionNew) {
  direction = directionNew;

  // Block automatic direction change
  if(directionNew == CLOCK_WISE) {
    automaticDirectionChangeToCounterclockwiseAllowed = false;
  } else if(directionNew == COUNTER_CLOCK_WISE) {
    automaticDirectionChangeToClockwiseAllowed = false;
  }
}

void motpos_ChangeByOne() {
  if (direction == COUNTER_CLOCK_WISE) {
    actualPosition += degreesPerPoint;
  } else if (direction == CLOCK_WISE) {
    actualPosition -= degreesPerPoint;
  }

  if (actualPosition >= 360) {
    actualPosition = 359;
  } else if (actualPosition < -360) {
    actualPosition = -360;
  }

  // Reactivate automatic direction change
  if(actualPosition >= 30) {
    automaticDirectionChangeToClockwiseAllowed = true;
  } else if(actualPosition <= 330) {
    automaticDirectionChangeToCounterclockwiseAllowed = true;
  }
}

void motpos_DoWork() {
  static unsigned long timePrevPosition = 0;
  static unsigned long timePrevZero = 0;
  const unsigned long debounceTimeMsPosition = 50;
  const unsigned long debounceTimeMsZero = 5000;
  unsigned long timeActual = millis();

  enDirection directionNew;

  // Handling of ISR Position
  if (isrPositionTriggered) {
    // Serial.print("ISR POSITION: timePrev=");
    // Serial.println(timePrevPosition);

    // Check if the interrupt is within the debounce window
    if (timeActual - timePrevPosition >= debounceTimeMsPosition) {
      timePrevPosition = timeActual;
      // Serial.println("ChangeByOne");
      motpos_ChangeByOne();
    }
    isrPositionTriggered = false;
  }

  // Handling of ISR Zero
  if (isrZeroTriggered) {
    // Check if the interrupt is within the debounce window
    if (timeActual - timePrevZero >= debounceTimeMsZero) {
      timePrevZero = timeActual;
      motpos_SetToZero();

      // Change direction if nescessary
      noOfTurns++;
      if (automaticDirectionChangeAllowed) {
        if (noOfTurns >= noOfTurnsPerDirection && ((direction == CLOCK_WISE && automaticDirectionChangeToCounterclockwiseAllowed) || (direction == COUNTER_CLOCK_WISE && automaticDirectionChangeToClockwiseAllowed))) {
          if (direction == CLOCK_WISE) {
            directionNew = COUNTER_CLOCK_WISE;
          } else {
            directionNew = CLOCK_WISE;
          }
          motpos_ChangeDirectionManually(directionNew);
          noOfTurns = 0;
        }
      }
    }
    isrZeroTriggered = false;
  }
}
