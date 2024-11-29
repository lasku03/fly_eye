﻿using System;

namespace Sensor_basics_serial_to_http
{
    internal class Program
    {
        static void Main(string[] args)
        {
            SerialComArduino serialComArduino = new SerialComArduino();

            HttpReceive httpReceive = new HttpReceive();
            httpReceive.Start();

            RelaySerial2Http relaySerial2Http = new RelaySerial2Http(serialComArduino, httpReceive);

            //TestSerialPortArduino testSerial = new TestSerialPortArduino(serialComArduino);
            //TestHttpRequest.TestSend();
            //TestHttpRequest.TestReceive();
            



            Console.WriteLine("Enter q to exit the programm...\n\n");
            while (Console.ReadKey().Key != ConsoleKey.Q) { } // never leave main

        }
    }
}