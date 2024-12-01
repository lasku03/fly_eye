using System;
using System.IO.Ports;
using System.Threading;

namespace Sensor_basics_serial_to_http
{
    internal class SerialComArduino
    {
        public event SerialComArduinoCmdEventHandler<SerialComArduinoCmdEventArgs> CmdReceivedEvent;
        public event SerialComArduinoDataEventHandler<SerialComArduinoDataEventArgs> DataChangedEvent;

        public string ComPort { get; private set; }
        public bool IsConnected
        {
            get { return SerialPort.IsOpen && arduinoConnectedAndInizialized; }
        }
        private bool arduinoConnectedAndInizialized = false;


        private SerialPort SerialPort = new SerialPort();
        private int baudRate = 115200;

        private int distanceMax = 1000;


        private string Angle { get; set; }
        private string Distance { get; set; }
        private string Direction { get; set; }


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

            // reset the Arduino by toggling the DTR line
            SerialPort.DtrEnable = false; // Drop DTR
            Thread.Sleep(100);           // Wait for 100ms
            SerialPort.DtrEnable = true; // Raise DTR

            // wait until Arduino drivers are initialized
            String responseArduino = String.Empty;
            while(responseArduino != "------Initialization done-------\r")
            {
                responseArduino = SerialPort.ReadLine();
                Console.WriteLine("Message received from microcontroller: " + responseArduino);
            }

            // start thread to read serial data continous
            Thread t = new Thread(SerialPortReadData);
            t.IsBackground = true;
            t.Name = "SerialComThread";
            t.Start();

            // set initialized flag
            arduinoConnectedAndInizialized = true;

        }

        public void SerialPortWriteCmd(enSerialComArduinoCmdTx cmd)
        {
            String cmdString = String.Empty;
            switch(cmd)
            {
                case enSerialComArduinoCmdTx.start:
                    cmdString = "start\r";
                    break;
                    case enSerialComArduinoCmdTx.stop:
                    cmdString = "stop\r";
                    break;
                    case enSerialComArduinoCmdTx.getready:
                    cmdString = "get ready\r";
                    break;
                case enSerialComArduinoCmdTx.scanperim:
                    cmdString = "start perimeter scan\r";
                    break;
                default:
                    throw new Exception("Serial Tx communication command does not exist");
            }
            SerialPort.WriteLine(cmdString);
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
                    ParseSerialInput(data);
                }
            } // never leave thread
        }

        private void ParseSerialInput(string data)
        {
            data = data.Trim();

            string[] substrings = data.Split(':');

            if (substrings[0] == "cmd")     // command received
            {
                ParseSerialCmd(substrings[1]);
            } else if(substrings[0] == "data")      // measurement data received
            {
                ParseSerialData(substrings[1]);
            }
            else
            {
                Console.WriteLine($"Message received from microcontroller: {substrings[0]}");
            }
        }

        private void ParseSerialCmd(string cmdString)
        {
            enSerialComArduinoCmdRx cmd = enSerialComArduinoCmdRx.n_a;
            if(cmdString == "ready")
            {
                cmd = enSerialComArduinoCmdRx.ready;
            } else if (cmdString == "perimeter scan done")
            {
                cmd = enSerialComArduinoCmdRx.perimio;
            } else
            {
                return;
            }

            OnCmdReceivedEvent(cmd);
        }

        private void ParseSerialData(string data)
        {
            // format string: "180;0.5;CLOCKWISE" (angle;distance;direction)
            string[] substrings = data.Split(';');

            string angle = substrings[0];
            string distance = substrings[1];
            string direction = substrings[2];

            // check angle
            if(!int.TryParse(angle, out int a))
            {
                return;
            }
            else if (a < 0 || a >= 360)
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
                d = distanceMax;
            }

            // check direction
            if(direction != "CLOCKWISE" && direction != "COUNTERCLOCKWISE")
            {
                return;
            }

            // check whether the values changed and fire event if so
            if (Angle != angle || Distance != distance || Direction != direction)
            {
                Angle = angle;
                Distance = distance;
                Direction = direction;
                OnDataChangedEvent();
            }
        }

        // events
        private void OnCmdReceivedEvent(enSerialComArduinoCmdRx cmd)
        {
            CmdReceivedEvent?.Invoke(this, new SerialComArduinoCmdEventArgs(cmd));
        }
        private void OnDataChangedEvent()
        {
            DataChangedEvent?.Invoke(this, new SerialComArduinoDataEventArgs(Angle, Distance, Direction));
        }
    }

    enum enSerialComArduinoCmdRx
    {
        n_a,
        ready,
        perimio
    }

    enum enSerialComArduinoCmdTx
    {
        n_a,
        start,
        stop,
        getready,
        scanperim
    }


    public delegate void SerialComArduinoDataEventHandler<SerialComArduinoDataEventArgs>(object sender, SerialComArduinoDataEventArgs e);
    public delegate void SerialComArduinoCmdEventHandler<SerialComArduinoCmdEventArgs>(object sender, SerialComArduinoCmdEventArgs e);
}
