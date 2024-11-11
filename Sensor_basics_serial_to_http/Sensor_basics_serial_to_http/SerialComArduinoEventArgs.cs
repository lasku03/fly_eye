using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal class SerialComArduinoEventArgs
    {
        public SerialComArduinoEventArgs(string angle, string distance)
        {
            Distance = distance;
            Angle = angle;
        }

        public string Angle { get; set; }
        public string Distance { get; set; }
    }
}
