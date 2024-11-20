using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal static class TestHttpRequest
    {
        public static void TestSend()
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

                HttpSend.SendData("localhost", angle, distance);
                Console.WriteLine($"\n HTTP request: http://localhost:8080/detections/{angle}/{distance}\n\n");
            }
        }

        public static void TestReceive()
        {
            HttpReceive httpReceive = new HttpReceive();
            httpReceive.Start();
            httpReceive.CmdReceivedEvent += CmdReceiveEventHandler;

            Console.WriteLine("Enter q to exit the test 'HTTP Receive'...\n\n");
            while (Console.ReadKey().Key != ConsoleKey.Q) { } // wait until 'q' is pressed

            httpReceive.Stop();
        }

        private static void CmdReceiveEventHandler(object sender, HttpReceiveCmdEventArgs e)
        {
            string command = e.Command;

            Console.WriteLine($"Received command over HTTP-request: {command}");
        }
    }
}
