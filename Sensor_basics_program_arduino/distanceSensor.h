/*
 * distanceSensor.h
 * 
 * Description: Driver for sensor reading. The driver initializes and reads the measured distance of an ultrasonic sensor of the type HC-SR04.
 * 
 * Author: Dominic Schinner
 * Date: 2024-11-11
 * Version: 1.0
 * 
 * Notes:
 * - Call Init() before using the driver.
 */

#ifndef DISTANCESENSOR_H_
#define DISTANCESENSOR_H_

// Hardware pins
#define DISTANCE_TRIG_PIN 5
#define DISTANCE_ECHO_PIN 4

// ---------
// Functions
// ---------

/**
 * @brief Initializes the driver.
 * 
 * @note Call this function in the setup code before using other driver functions of this module.
 */
void distsens_Init();

/**
 * @brief Get the last valid distance read by the sensor.
 *
 * @return Returns the distance in mm.
 *
 * @pre Inititalization of the driver with the function Init().
 * @pre To get actual values the function ReadDuration() should be called before.
 */
float distsens_GetDistance();

/**
 * @brief Convert a given time of flight in microseconds into a distance. The used speed of sound is 343m/s.
 *
 * @param timeOfFlight Time to be converted in microseconds.
 *
 * @return Converts a duration of sound into a distance.
 *
 */
float distsens_TimeOfFlightToDistance(float timeOfFlight);

/**
 * @brief Start a measurement of the actual time of flight from the sensor to run in a hyperloop and store the value into a global variable.
 *
 * @pre Inititalization of the driver with the function Init().
 * @post Get measured value with the function GetDistance().
 */
void distsens_DoWork();


















#endif