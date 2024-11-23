/*
 * motor.h
 * 
 * Description: The module provides functions to control a motor with 360°-position measurement.
 * 
 * Author: Dominic Schinner
 * Date: 2024-10-30
 * Version: 1.0
 * 
 * Notes:
 * - Call Init() before using the module.
 */

#ifndef MOTOR_H_
#define MOTOR_H_

// Enums
enum enMotorState {
  MOT_STOP,
  MOT_RUN,
  MOT_BREAK,
  MOT_WAIT
};

// ---------
// Functions
// ---------

/**
 * @brief Initializes the module.
 * 
 * @note Call this function in the setup code before using other functions of this module.
 */
void mot_Init();

/**
 * @brief Start the motor. The execution for a hyperloop runs in the DoWork() function.
 *
 * @pre Inititalization of the driver with the function Init().
 * @post Execute the function DoWork() in the hyperloop.
 */
void mot_Run();

/**
 * @brief Stop the motor. The execution for a hyperloop runs in the DoWork() function.
 *
 * @pre Inititalization of the driver with the function Init().
 * @post Execute the function DoWork() in the hyperloop.
 */
void mot_Stop();

/**
 * @brief Execution of motor functionality to run in a hyperloop.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void mot_DoWork();

#endif