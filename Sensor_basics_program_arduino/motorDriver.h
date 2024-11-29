/*
 * motorDriver.h
 * 
 * Description: Driver for a motor with hardware driver. The driver supports enabling the motor, PWM for the speed and an output for the change of direction.
 * 
 * Author: Dominic Schinner
 * Date: 2024-10-30
 * Version: 1.0
 * 
 * Notes:
 * - Call Init() before using the driver.
 */
 

#ifndef MOTORDRIVER_H_
#define MOTORDRIVER_H_

// includes
#include <stdint.h>

// Hardware pins
#define IN1_PIN 9
#define IN2_PIN 8
#define PWM_PIN 10

// Enums
enum enDirection {
  CLOCK_WISE,
  COUNTER_CLOCK_WISE
};

// ---------
// Functions
// ---------

/**
 * @brief Initializes the driver.
 * 
 * @note Call this function in the setup code before using other driver functions of this module.
 */
void motdriv_Init();

/**
 * @brief Drive the motor.
 *
 * @param speed Adjust speed in %.
 * @param direction Specifie direction (CLOCK_WISE or COUNTER_CLOCK_WISE).
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motdriv_Drive(uint8_t speed, enDirection direction);

/**
 * @brief Stops the motor.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motdriv_Stop();

#endif