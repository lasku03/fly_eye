using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal static class HttpSendData
    {
        private static readonly HttpClient client = new HttpClient();

        public static void SendGetRequest(string host, string angle, string distance)
        {
            try
            {
                /*string url = $"http://localhost:8080/detections/{angle}/{distance}";*/
                string url = $"http://{host}:8080/detections/{angle}/{distance}";
                HttpResponseMessage response = client.GetAsync(url).Result;

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error: {response.StatusCode}");
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Exception caught: {e.Message}");
            }
        }

        public static void SendPostRequest(string host, string angle, string distance)
        {
            try
            {
                /*string url = $"http://localhost:8080/detections/{angle}/{distance}";*/
                string url = $"http://{host}:8080/detections/{angle}/{distance}";
                HttpResponseMessage response = client.PostAsync(url, null).Result;

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error: {response.StatusCode}");
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Exception caught: {e.Message}");
            }
        }

    }
}
