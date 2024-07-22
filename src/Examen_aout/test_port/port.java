package Examen_aout.test_port;

import java.io.InputStream;
import java.util.Scanner;

import com.fazecast.jSerialComm.*;

public class port {

    /**
     * This class provides a test case for the jSerialComm library.
     *
     * @see java.io.InputStream
     * @see java.io.OutputStream
     */

    private static final class PacketListener implements SerialPortPacketListener {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] newData = event.getReceivedData();
            System.out.println("Received data of size: " + newData.length);
            for (int i = 0; i < newData.length; ++i)
                System.out.print((char)newData[i]);
            System.out.println("\n");
        }
        @Override
        public int getPacketSize() { return 100; }
    }

    private static final class MessageListener implements SerialPortMessageListener {
        public String byteToHex(byte num) {
            char[] hexDigits = new char[2];
            hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
            hexDigits[1] = Character.forDigit((num & 0xF), 16);
            return new String(hexDigits);
        }
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] byteArray = event.getReceivedData();
            StringBuffer hexStringBuffer = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++)
                hexStringBuffer.append(byteToHex(byteArray[i]));
            System.out.println("Received the following message: " + hexStringBuffer.toString());
        }
        @Override
        public byte[] getMessageDelimiter() { return new byte[]{ (byte)0xB5, (byte)0x62 }; }
        @Override
        public boolean delimiterIndicatesEndOfMessage() { return false; }
    }
        static public void main(String[] args)
        {
            SerialPort.allowPortOpenForEnumeration();
            SerialPort.autoCleanupAtShutdown();
            SerialPort.addShutdownHook(new Thread() { public void run() { System.out.println("\nRunning shutdown hook"); } });
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("\nAvailable Ports:\n");
            for (int i = 0; i < ports.length; ++i)
                System.out.println("   [" + i + "] " + ports[i].getSystemPortName() + " (" + ports[i].getSystemPortPath() + "): " + ports[i].getDescriptivePortName() + " - " + ports[i].getPortDescription() + " @ " + ports[i].getPortLocation() + " (VID = " + ports[i].getVendorID() + ", PID = " + ports[i].getProductID() + ", Serial = " + ports[i].getSerialNumber() + ", Manufacturer = " + ports[i].getManufacturer() + ")");
            System.out.println("\nRe-enumerating ports again in 2 seconds...\n");
            try { Thread.sleep(2000); } catch (Exception e) {}
            ports = SerialPort.getCommPorts();
            System.out.println("Available Ports:\n");
            for (int i = 0; i < ports.length; ++i)
                System.out.println("   [" + i + "] " + ports[i].getSystemPortName() + " (" + ports[i].getSystemPortPath() + "): " + ports[i].getDescriptivePortName() + " - " + ports[i].getPortDescription() + " @ " + ports[i].getPortLocation() + " (VID = " + ports[i].getVendorID() + ", PID = " + ports[i].getProductID() + ", Serial = " + ports[i].getSerialNumber() + ", Manufacturer = " + ports[i].getManufacturer() + ")");
            SerialPort ubxPort;
            System.out.print("\nChoose your desired serial port or enter -1 to specify a port directly: ");
            int serialPortChoice = -2;
            Scanner inputScanner = new Scanner(System.in);
            try {
                serialPortChoice = inputScanner.nextInt();
            } catch (Exception e) {}
            if (serialPortChoice == -2)
            {
                inputScanner.close();
                return;
            }
            else if (serialPortChoice == -1)
            {
                String serialPortDescriptor = "";
                System.out.print("\nSpecify your desired serial port descriptor: ");
                try {
                    while (serialPortDescriptor.isEmpty())
                        serialPortDescriptor = inputScanner.nextLine();
                } catch (Exception e) { e.printStackTrace(); }
                ubxPort = SerialPort.getCommPort(serialPortDescriptor);
            }
            else
                ubxPort = ports[serialPortChoice];
            ubxPort.allowElevatedPermissionsRequest();
            byte[] readBuffer = new byte[2048];
            System.out.println("\nPre-setting RTS: " + (ubxPort.setRTS() ? "Success" : "Failure"));
            boolean openedSuccessfully = ubxPort.openPort(0);
            System.out.println("\nOpening " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + " - " + ubxPort.getPortDescription() + ": " + openedSuccessfully);
            if (!openedSuccessfully)
            {
                System.out.println("Error code was " + ubxPort.getLastErrorCode() + " at Line " + ubxPort.getLastErrorLocation());
                inputScanner.close();
                return;
            }
            System.out.println("Setting read timeout mode to non-blocking");
            ubxPort.setBaudRate(115200);
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 1000, 0);
            try
            {
                for (int i = 0; i < 3; ++i)
                {
                    System.out.println("\nReading #" + i);
                    System.out.println("Available: " + ubxPort.bytesAvailable());
                    int numRead = ubxPort.readBytes(readBuffer, readBuffer.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nSetting read timeout mode to semi-blocking with a timeout of 200ms");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 200, 0);
            try
            {
                for (int i = 0; i < 3; ++i)
                {
                    System.out.println("\nReading #" + i);
                    System.out.println("Available: " + ubxPort.bytesAvailable());
                    int numRead = ubxPort.readBytes(readBuffer, readBuffer.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nSetting read timeout mode to semi-blocking with no timeout");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            System.out.println("\nWaiting for available bytes to read...");
            while (ubxPort.bytesAvailable() == 0);
            System.out.println("Available: " + ubxPort.bytesAvailable());
            System.out.println("Flushing read buffers: " + ubxPort.flushIOBuffers());
            try
            {
                for (int i = 0; i < 3; ++i)
                {
                    System.out.println("\nReading #" + i);
                    System.out.println("Available: " + ubxPort.bytesAvailable());
                    int numRead = ubxPort.readBytes(readBuffer, readBuffer.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nSetting read timeout mode to blocking with a timeout of 100ms");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 0);
            try
            {
                for (int i = 0; i < 3; ++i)
                {
                    System.out.println("\nReading #" + i);
                    System.out.println("Available: " + ubxPort.bytesAvailable());
                    int numRead = ubxPort.readBytes(readBuffer, readBuffer.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nSetting read timeout mode to blocking with no timeout");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            try
            {
                for (int i = 0; i < 3; ++i)
                {
                    System.out.println("\nReading #" + i);
                    System.out.println("Available: " + ubxPort.bytesAvailable());
                    int numRead = ubxPort.readBytes(readBuffer, readBuffer.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nSwitching over to event-based reading");
            System.out.println("\nListening for any amount of data available\n");
            ubxPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    SerialPort comPort = event.getSerialPort();
                    System.out.println("Available: " + comPort.bytesAvailable() + " bytes.");
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            });
            try { Thread.sleep(5000); } catch (Exception e) {}
            ubxPort.removeDataListener();
            System.out.println("\nNow listening for full 100-byte data packets\n");
            PacketListener listener = new PacketListener();
            ubxPort.addDataListener(listener);
            try { Thread.sleep(5000); } catch (Exception e) {}
            ubxPort.removeDataListener();
            System.out.println("\nNow listening for byte-delimited binary messages\n");
            MessageListener messageListener = new MessageListener();
            ubxPort.addDataListener(messageListener);
            try { Thread.sleep(5000); } catch (Exception e) {}
            ubxPort.removeDataListener();
            System.out.println("\n\nClosing " + ubxPort.getDescriptivePortName() + ": " + ubxPort.closePort());
            try { Thread.sleep(1000); } catch (InterruptedException e1) { e1.printStackTrace(); }
            System.out.println("Reopening " + ubxPort.getDescriptivePortName() + ": " + ubxPort.openPort() + "\n");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
            InputStream in = ubxPort.getInputStream();
            try
            {
                for (int j = 0; j < 1000; ++j)
                    System.out.print((char)in.read());
                in.close();
            } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\n\nClosing " + ubxPort.getDescriptivePortName() + ": " + ubxPort.closePort());
            openedSuccessfully = ubxPort.openPort(0);
            System.out.println("Reopening " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + ": " + openedSuccessfully);
            if (!openedSuccessfully)
            {
                inputScanner.close();
                return;
            }
            System.out.println("\n\nReading for 5 seconds then closing from a separate thread...");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            final SerialPort finalPort = ubxPort;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    byte[] buffer = new byte[2048];
                    while (finalPort.isOpen())
                    {
                        System.out.println("\nStarting blocking read...");
                        int numRead = finalPort.readBytes(buffer, buffer.length);
                        System.out.println("Read " + numRead + " bytes");
                    }
                    System.out.println("\nPort was successfully closed from a separate thread");
                }
            });
            thread.start();
            try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }
            System.out.println("\nClosing " + ubxPort.getDescriptivePortName() + ": " + ubxPort.closePort());
            try { thread.join(); } catch (Exception e) { e.printStackTrace(); }
            openedSuccessfully = ubxPort.openPort(0);
            System.out.println("\nReopening " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + ": " + openedSuccessfully);
            if (!openedSuccessfully)
            {
                inputScanner.close();
                return;
            }
            System.out.println("\n\nNow waiting asynchronously for all possible listening events...");
            ubxPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_PARITY_ERROR | SerialPort.LISTENING_EVENT_DATA_WRITTEN | SerialPort.LISTENING_EVENT_BREAK_INTERRUPT |
                        SerialPort.LISTENING_EVENT_CARRIER_DETECT | SerialPort.LISTENING_EVENT_CTS | SerialPort.LISTENING_EVENT_DSR | SerialPort.LISTENING_EVENT_RING_INDICATOR | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED |
                        SerialPort.LISTENING_EVENT_FRAMING_ERROR | SerialPort.LISTENING_EVENT_FIRMWARE_OVERRUN_ERROR | SerialPort.LISTENING_EVENT_SOFTWARE_OVERRUN_ERROR | SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    System.out.println("Received event type: " + event.toString());
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    {
                        byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
                        event.getSerialPort().readBytes(buffer, buffer.length);
                        System.out.println("   Reading " + buffer.length + " bytes");
                    }
                }
            });
            try { Thread.sleep(5000); } catch (Exception e) {}
            ubxPort.removeDataListener();
            ubxPort.closePort();
            openedSuccessfully = ubxPort.openPort(0);
            System.out.println("\nReopening " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + ": " + openedSuccessfully);
            if (!openedSuccessfully)
            {
                inputScanner.close();
                return;
            }
            System.out.println("\n\nUnplug the device sometime in the next 10 seconds to ensure that it closes properly...\n");
            ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            ubxPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    SerialPort comPort = event.getSerialPort();
                    System.out.println("Available: " + comPort.bytesAvailable() + " bytes.");
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);
                    System.out.println("Read " + numRead + " bytes.");
                }
            });
            try { Thread.sleep(10000); } catch (Exception e) {}
            ubxPort.removeDataListener();
            System.out.println("\nClosing " + ubxPort.getDescriptivePortName() + ": " + ubxPort.closePort());


            inputScanner.close();
        }
    }

