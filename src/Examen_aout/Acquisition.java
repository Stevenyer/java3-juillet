package Examen_aout;

import ExamenJava3_janvier_1.Message;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Acquisition extends Thread {
    private static SerialPort serialPort = null;
    private static String p ;
    private static int baudRate;
    private JComboBox<String> comboBox1;
    private JPanel portlist;
    private JButton portbtn;
    private static BufferedReader input = null;
    private static OutputStream out;
    private static ObjectOutputStream ouut;

    public static void main(String[] args) {
        new Acquisition().start();
    }

    @Override
    public void run() {
        System.out.println("Waiting for Client...");
        try (ServerSocket server = new ServerSocket(6000);){
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client Connected");
                data(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static SerialPort getPort(Socket socket) {
        try {
            if (input == null) {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            SerialPort port = SerialPort.getCommPort(input.readLine());
            System.out.println(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void send(String data, String time, Socket socket) {
        try {
            if (ouut == null) {
                out = socket.getOutputStream();
                ouut = new ObjectOutputStream(out);
            }
            ouut.writeObject(new Message(data, time));
            ouut.flush(); // Ensure that the object is completely written to the stream
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }
    private static void data(Socket socket) {
        try (Socket s = socket;) {
            SerialPort ports = getPort(socket);
            if (ports.openPort()) {
                ports.setComPortParameters(baudRate, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                ports.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                input = new BufferedReader(new InputStreamReader(ports.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    //System.out.println("Received from arduino : " + line);
                    String currentTime = getCurrentTime();
                    String dataToSend = "Time: " + currentTime + ", Arduino Data: " + line;
                    System.out.println(dataToSend);
                    send(line, currentTime, socket);
                }
            } else {
                System.out.println("Error: Unable to open the serial port.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
