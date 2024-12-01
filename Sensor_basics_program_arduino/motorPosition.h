/*
 * motorPosition.h
 * 
 * Description: Driver for motor position measurement. The driver supports tracking of the motor position around 360° with two digital position sensors.
 * 
 * Author: Dominic Schinner
 * Date: 2024-10-30
 * Version: 1.0
 * 
 * Notes:
 * - Call Init() before using the driver.
 */

#ifndef MOTORPOSITION_H_
#define MOTORPOSITION_H_

// includes
#include <stdint.h>
#include "motorDriver.h"

// Hardware pins
#define POSITION_PIN 3
#define POSITION_REF_PIN 2

// ---------
// Functions
// ---------

/**
 * @brief Initializes the driver.
 * 
 * @note Call this function in the setup code before using other driver functions of this module.
 */
void motpos_Init();

/**
 * @brief Get the actual position.
 *
 * @return Returns the actual motor position in degrees (0-360°).
 *
 * @pre Inititalization of the driver with the function Init().
 */
uint16_t motpos_GetActual();

/**
 * @brief Get the actual direction.
 *
 * @return Returns the actual motor direction (CLOCK_WISE or COUNTER_CLOCK_WISE).
 *
 * @pre Inititalization of the driver with the function Init().
 */
enDirection motpos_GetDirection();

/**
 * @brief Set the stored value for the actual position manually to zero.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motpos_SetToZero();

/**
 * @brief Enable or disables the automatic direction change if the ISR is triggered.
 *
 * @param en True enables the automatic direction change, false disables it.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motpos_ChangeDirectionAutomaticallyEnable(bool en);

/**
 * @brief Change the direction manually.
 *
 * @param direction New direction.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motpos_ChangeDirectionManually(enDirection directionNew);

/**
 * @brief Increment or decrement the actual position depending on the direction. This function should be called out of the ISR.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void motpos_ChangeByOne();

/**
 * @brief Debounce and handle ISR logic for the sensor inputs in a hyperloop.
 *
 * @pre Inititalization of the driver with the function Init().
 * @post Handles Debouncing of ISR logic.
 */
void motpos_DoWork();

#endif