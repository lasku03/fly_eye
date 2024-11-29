/*
 * stateMachine.h
 * 
 * Description: The module contains the logic for the state machine.
 * 
 * Author: Dominic Schinner
 * Date: 2024-11-25
 * Version: 1.0
 * 
 */

#ifndef STATEMACHINE_H_
#define STATEMACHINE_H_

// ---------
// Functions
// ---------

/**
 * @brief Sends zero flag to stateMachine.
 *
 */
void sm_SetZeroPositionFlag();

/**
 * @brief Execution of state machine functionality to run in a hyperloop.
 *
 */
void sm_DoWork();

#endif