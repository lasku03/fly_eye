using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal class RelaySerial2Http
    {
        private string host = "localhost";

        public RelaySerial2Http(SerialComArduino serialComArduino)
        {
            // choose host address
            Console.WriteLine("Enter host IP-address. Enter 'l' + 'enter' for localhost.");
            string input = Console.ReadLine();
            if(input == "l")    // localhost
            {
                input = "localhost";
            }
            host = input;
            Console.WriteLine($"Host address adjusted to {host}\n\n");

            // register data changed event
            serialComArduino.DataChangedEvent += DataChangedEventHandler;
        }



        private void DataChangedEventHandler(object sender, SerialComArduinoEventArgs e)
        {
            string angle = e.Angle;
            string distance = e.Distance;

            /*HttpSendData.SendGetRequest(host, angle, distance);*/
            HttpSend.SendData(host, angle, distance);
        }
    }
}
