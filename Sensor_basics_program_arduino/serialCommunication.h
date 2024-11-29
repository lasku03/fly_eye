/*
 * serialCommunication.h
 * 
 * Description: Driver for serial communication over USB.
 * 
 * Author: Dominic Schinner
 * Date: 2024-11-11
 * Version: 1.0
 * 
 * Notes:
 * - Call Init() before using the driver.
 */

#ifndef SERIALCOMMUNICATION_H_
#define SERIALCOMMUNICATION_H_

// includes
#include <stdint.h>
#include <string.h>
#include "motorDriver.h"

#define SERIAL_BAUD_RATE 115200

// Enums
enum enSerRxCmd {
  START,    // Serial cmd: start\l\n
  STOP,     // Serial cmd: stop\l\n
  GETREADY, // Serial cmd: get ready\l\n
  SCANPERIM // Serial cmd: start perimeter scan\l\n
};

enum enSerTxCmd {
  READY,    // Serial cmd: ready\l\n
  PERIMIO   // Serial cmd: perimeter scan done\l\n
};

// ---------
// Functions
// ---------

/**
 * @brief Initializes the driver.
 * 
 * @note Call this function in the setup code before using other driver functions of this module.
 */
void sercom_Init();

/**
 * @brief Send distance, position and direction to the serial port.
 *
 * @param angle Actual angle in degrees between 0 and 360.
 * @param distance Measurement value in mm
 * @param direction Actual driving direction (CLOCKWISE or COUNTERCLOCKWISE)
 *
 * @pre Inititalization of the driver with the function Init().
 */
void sercom_WriteData(uint16_t angle, float distance, enDirection direction);

/**
 * @brief Send a command to the serial port.
 *
 * @param cmd Control command to be sent.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void sercom_WriteCmd(enSerTxCmd cmd);

/**
 * @brief Read a command from the serial port and wait until a cmd received.
 *
 * @return Returns the actual command and blocks.
 *
 * @pre Inititalization of the driver with the function Init().
 */
enSerRxCmd sercom_ReadCmdWait();

/**
 * @brief Read a command from the serial port without blocking. If no command is available returns "n/a".
 *
 * @return Returns the actual command without blocking.
 *
 * @pre Inititalization of the driver with the function Init().
 */
enSerRxCmd sercom_ReadCmdWithoutWaiting();

/**
 * @brief Wait until a specific command was received.
 *
 * @param cmdExp Expected command to wait for.
 *
 * @pre Inititalization of the driver with the function Init().
 */
void sercom_WaitForCmd(enSerRxCmd cmdExp);











#endif