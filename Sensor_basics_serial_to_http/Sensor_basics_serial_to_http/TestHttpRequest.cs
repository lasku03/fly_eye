using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal static class TestHttpRequest
    {
        public static void Test()
        {
            while (true)
            {
                Console.Write("Enter value for angle (enter q to quit): ");
                string angle = Console.ReadLine();
                if(angle == "q")    // exit if entered value is q
                {
                    return;
                }
                Console.Write("Enter value for distance: ");
                string distance = Console.ReadLine();

                HttpSendData.SendGetRequest("localhost", angle, distance);
                Console.WriteLine($"\n HTTP request: http://localhost:8080/detections/{angle}/{distance}\n\n");
            }
        }
    }
}
