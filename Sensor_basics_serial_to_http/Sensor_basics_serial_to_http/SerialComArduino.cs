using System;
using System.IO.Ports;
using System.Threading;

namespace Sensor_basics_serial_to_http
{
    internal class SerialComArduino
    {
        public event SerialComArduinoEventHandler<SerialComArduinoEventArgs> DataChangedEvent;
        public string ComPort { get; private set; }


        private SerialPort SerialPort = new SerialPort();
        private int baudRate = 115200;

        private int distanceMax = 10000;


        private string Angle { get; set; }
        private string Distance { get; set; }


        public SerialComArduino()
        {
            Console.WriteLine("Initialize serial port...");

            // read serial com-port from console
            string input = null;
            int inputInt = 0;
            bool err = false;

            while (!err)
            {
                Console.WriteLine("Enter a number for serial COM-port.");
                input = Console.ReadLine();
                // check input
                if (!int.TryParse(input, out inputInt))
                {
                    Console.WriteLine("The entered value is not a number! \n");
                    continue;
                }
                else if (inputInt == 0)
                {
                    Console.WriteLine("The COM-port can not be 0! \n");
                    continue;
                }
                ComPort = "COM" + inputInt;

                // set serial port properties
                SerialPort.PortName = ComPort;
                SerialPort.BaudRate = baudRate;

                // connect to arduino
                try
                {
                    SerialPort.Open();
                }
                catch
                {
                    Console.WriteLine("Unable to connect to serial port... \n");
                    continue;
                }
                err = true;
            }
            Console.WriteLine("Arduino successful connected to serial port " + ComPort + " @" + baudRate + "\n\n");

            SerialPort.ReadExisting();  // free up serial buffer

            // start thread to read serial data continous
            Thread t = new Thread(SerialPortReadData);
            t.IsBackground = true;
            t.Name = "SerialComThread";
            t.Start();

        }

        // thread read data from serial port
        private void SerialPortReadData()
        {
            while (true)
            {
                if (SerialPort.IsOpen)
                {
                    string data = SerialPort.ReadLine();
                    /*Console.WriteLine("Data received: " + data);*/
                    ParseSerialData(data);
                }
            } // never leave thread
        }

        private void ParseSerialData(string data)
        {
            // format string: "180;0.5" (angle;distance)
            string[] substrings = data.Split(';');

            string angle = substrings[0];
            string distance = substrings[1];

            // check angle
            if(!int.TryParse(angle, out int a))
            {
                return;
            }
            else if (a < 0 || a > 360)
            {
                return;
            }

            // check distance
            if (!float.TryParse(distance, out float d))
            {
                return;
            }
            else if (d < 0 || d > distanceMax)
            {
                return;
            }

            // check wheter the values changed and fire event if so
            if (Angle != angle || Distance != distance)
            {
                Angle = angle;
                Distance = distance;
                OnDataChangedEvent();
            }
        }

        // data changed event
        private void OnDataChangedEvent()
        {
            DataChangedEvent?.Invoke(this, new SerialComArduinoEventArgs(Angle, Distance));
        }
    }

    public delegate void SerialComArduinoEventHandler<SerialComArduinoEventArgs>(object sender, SerialComArduinoEventArgs e);
}
