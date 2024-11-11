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
            // register data changed event
            serialComArduino.DataChangedEvent += DataChangedEventHandler;
        }

        private void DataChangedEventHandler(object sender, SerialComArduinoEventArgs e)
        {
            string angle = e.Angle;
            string distance = e.Distance;

            Console.WriteLine($"angle: {angle} ; distance: {distance}");
        }
    }
}
