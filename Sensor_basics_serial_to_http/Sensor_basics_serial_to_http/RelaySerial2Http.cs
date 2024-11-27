using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Sensor_basics_serial_to_http
{
    internal class RelaySerial2Http
    {
        private string host = "localhost";
        private SerialComArduino serialComArduino;
        private HttpReceive httpReceive;
        private enSendHttpMode sendHttpMode = enSendHttpMode.NO;
        private enState state = enState.INIT;
        private enState stateBeforeWait = enState.INIT;

        private string Angle { get; set; }

        private enum enState
        {
            INIT,
            WAIT,
            CONNECT,
            GETREADY,
            READY,
            STARTPERIM,
            WAITPERIM,
            PERIM,
            PERIMSCANDONE,
            RUN,
            STOP
        }

        private enum enSendHttpMode
        {
            NO,
            MEASUREMENT,
            PERIMETER
        }

        public RelaySerial2Http(SerialComArduino serialComArduino, HttpReceive httpReceive)
        {
            // initialize communications
            this.serialComArduino = serialComArduino;
            this.httpReceive = httpReceive;

            // choose host address
            Console.WriteLine("Enter host IP-address. Enter 'l' + 'enter' for localhost.");
            string input = Console.ReadLine();
            if (input == "l")    // localhost
            {
                input = "localhost";
            }
            host = input;
            Console.WriteLine($"Host address adjusted to {host}\n\n");

            // register events
            this.serialComArduino.CmdReceivedEvent += SerialCmdReceivedEventHandler;
            this.serialComArduino.DataChangedEvent += DataChangedEventHandler;
            this.httpReceive.CmdReceivedEvent += HttpReceivedEventHandler;


            // start thread to read serial data continous
            Thread t = new Thread(StateMachine);
            t.IsBackground = true;
            t.Name = "RelaySerial2HttpTask";
            t.Start();
        }

        // thread
        private void StateMachine()
        {
            while (true)
            {
                // state machine
                switch (state)
                {
                    case enState.INIT:
                        sendHttpMode = enSendHttpMode.NO;
                        state = enState.CONNECT;
                        break;
                    case enState.WAIT:
                        break;
                    case enState.CONNECT:
                        if (serialComArduino.IsConnected)
                        {
                            state = enState.GETREADY;
                        }
                        break;
                    case enState.GETREADY:
                        stateBeforeWait = enState.GETREADY;
                        serialComArduino.SerialPortWriteCmd(enSerialComArduinoCmdTx.getready);
                        Console.WriteLine("Wait until hardware is ready...");
                        state = enState.WAIT;
                        break;
                    case enState.READY:
                        sendHttpMode = enSendHttpMode.NO;
                        stateBeforeWait = enState.READY;
                        HttpSend.SendCmd(host, "ready");
                        Console.WriteLine("Wait until a start command (\'start/perimeters' or 'start/dedections\') over HTTP is received...");
                        state = enState.WAIT;
                        break;
                    case enState.STARTPERIM:
                        sendHttpMode = enSendHttpMode.NO;
                        serialComArduino.SerialPortWriteCmd(enSerialComArduinoCmdTx.scanperim);
                        Console.WriteLine("Perimeter scan started");
                        Console.WriteLine("Wait until angle value is zero...");
                        state = enState.WAITPERIM;
                        break;
                    case enState.WAITPERIM:
                        if (Angle == "0")
                        {
                            state = enState.PERIM;
                        }
                        break;
                    case enState.PERIM:
                        sendHttpMode = enSendHttpMode.PERIMETER;
                        stateBeforeWait = enState.PERIM;
                        Console.WriteLine("Wait until perimter scan is done...");
                        state = enState.WAIT;
                        break;
                    case enState.PERIMSCANDONE:
                        Console.WriteLine("Perimeter scan done");
                        sendHttpMode |= enSendHttpMode.NO;
                        stateBeforeWait = enState.PERIMSCANDONE;
                        Console.WriteLine("Wait until a start command (\'start/perimeters' or 'start/dedections\') over HTTP is received...");
                        state = enState.WAIT;
                        break;
                    case enState.RUN:
                        Console.WriteLine("Measurement started. Wait until command \'stop\' over HTTP is received...");
                        sendHttpMode = enSendHttpMode.MEASUREMENT;
                        stateBeforeWait = enState.RUN;
                        serialComArduino.SerialPortWriteCmd(enSerialComArduinoCmdTx.start);
                        state = enState.WAIT;
                        break;
                    case enState.STOP:
                        Console.WriteLine("Measurement stopped");
                        sendHttpMode = enSendHttpMode.NO;
                        stateBeforeWait = enState.STOP;
                        serialComArduino.SerialPortWriteCmd(enSerialComArduinoCmdTx.stop);
                        Console.WriteLine("Wait until a start command (\'start/perimeters' or 'start/dedections\') over HTTP is received...");
                        state = enState.WAIT;
                        break;
                    default:
                        throw new Exception("Unknown state in state machine");
                }
            } // never leave thread
        }

        private void ChangeStateToReady()
        {
            // Define allowed steps for the current operation
            HashSet<enState> allowedStates = new HashSet<enState>
            {
                enState.GETREADY
            };

            if(allowedStates.Contains(stateBeforeWait))
            {
                state = enState.READY;
            }
        }

        private void ChangeStateToStartPerim()
        {
            // Define allowed steps for the current operation
            HashSet<enState> allowedStates = new HashSet<enState>
            {
                enState.READY,
                enState.STOP,
                enState.PERIMSCANDONE
            };

            if (allowedStates.Contains(stateBeforeWait))
            {
                state = enState.STARTPERIM;
            }
        }

        private void ChangeStateToPerimScanDone()
        {
            // Define allowed steps for the current operation
            HashSet<enState> allowedStates = new HashSet<enState>
            {
                enState.PERIM
            };

            if (allowedStates.Contains(stateBeforeWait))
            {
                state = enState.PERIMSCANDONE;
            }
        }

        private void ChangeStateToRun()
        {
            // Define allowed steps for the current operation
            HashSet<enState> allowedStates = new HashSet<enState>
            {
                enState.READY,
                enState.STOP,
                enState.PERIMSCANDONE
            };

            if (allowedStates.Contains(stateBeforeWait))
            {
                state = enState.RUN;
            }
        }

        private void ChangeStateToStop()
        {
            // Define allowed steps for the current operation
            HashSet<enState> allowedStates = new HashSet<enState>
            {
                enState.RUN
            };

            if (allowedStates.Contains(stateBeforeWait))
            {
                state = enState.STOP;
            }
        }


        private void SerialCmdReceivedEventHandler(object sender, SerialComArduinoCmdEventArgs e)
        {
            enSerialComArduinoCmdRx cmd = e.Cmd;

            switch(cmd)
            {
                case enSerialComArduinoCmdRx.n_a:
                    break;
                case enSerialComArduinoCmdRx.ready:
                    ChangeStateToReady();
                    break;
                case enSerialComArduinoCmdRx.perimio:
                    ChangeStateToPerimScanDone();
                    break;
                default:
                    throw new Exception("Unknown command received from serial");
            }
        }

        private void DataChangedEventHandler(object sender, SerialComArduinoDataEventArgs e)
        {
            string angle = e.Angle;
            string distance = e.Distance;
            string direction = e.Direction;

            Angle = e.Angle;

            switch (sendHttpMode)
            {
                case enSendHttpMode.NO:
                    break;
                case enSendHttpMode.MEASUREMENT:
                    HttpSend.SendData(host, angle, distance, direction);
                    break;
                case enSendHttpMode.PERIMETER:
                    HttpSend.SendData(host, angle, distance, direction, "perimeters");
                    break;
                default:
                    break;
            }
        }

        private void HttpReceivedEventHandler(object sender, HttpReceiveCmdEventArgs e)
        {
            enHttpReceiveCmd cmd = e.Command;

            switch(cmd)
            {
                case enHttpReceiveCmd.n_a:
                    break;
                case enHttpReceiveCmd.start_dedections:
                    ChangeStateToRun();
                    break;
                case enHttpReceiveCmd.start_perimeters:
                    ChangeStateToStartPerim();
                    break;
                case enHttpReceiveCmd.stop:
                    ChangeStateToStop();
                    break;
                default:
                    throw new Exception("Unknown command received from HTTP");
            }
        }
    }
}
