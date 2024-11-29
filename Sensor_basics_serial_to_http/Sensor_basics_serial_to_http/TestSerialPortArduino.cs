using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal class TestSerialPortArduino
    {
        public TestSerialPortArduino(SerialComArduino serialComArduino)
        {
            // register events
            serialComArduino.CmdReceivedEvent += CmdReceivedEventHandler;
            serialComArduino.DataChangedEvent += DataChangedEventHandler;
        }

        private void CmdReceivedEventHandler(object sender, SerialComArduinoCmdEventArgs e)
        {
            enSerialComArduinoCmdRx cmd = e.Cmd;

            Console.WriteLine($"cmd: {cmd}");
        }

        private void DataChangedEventHandler(object sender, SerialComArduinoDataEventArgs e)
        {
            string angle = e.Angle;
            string distance = e.Distance;
            string direction = e.Direction;

            Console.WriteLine($"angle: {angle} ; distance: {distance} ; direction: {direction}");
        }
    }
}
