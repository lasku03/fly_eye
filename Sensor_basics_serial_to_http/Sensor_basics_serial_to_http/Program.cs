using System;

namespace Sensor_basics_serial_to_http
{
    internal class Program
    {
        static void Main(string[] args)
        {
            SerialComArduino serialComArduino = new SerialComArduino();
            RelaySerial2Http relaySerial2Http = new RelaySerial2Http(serialComArduino);

            TestSerialPortArduino testSerial = new TestSerialPortArduino(serialComArduino);
            //TestHttpRequest.Test();




            Console.WriteLine("Enter q to exit the programm...\n\n");
            while (Console.ReadKey().Key != ConsoleKey.Q) { } // never leave main

        }
    }
}