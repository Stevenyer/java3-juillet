package Examen_aout;

import Examen_aout.Message;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
public class Client {
private JComboBox<String> comboBox1;
private JButton portbtn;
private JPanel panel1;
private JLabel result, valueLabel, timeLabel, timestampLabel;
private BufferedReader input;
private int baudRate = 9600;
private Socket socket;
private ObjectOutputStream oos;
private ObjectInputStream ois;
private static Message received;
private static String data;
private static String time;
private SerialPort port;
private SerialPort selectedPort;


public Client() {

//    try {
//        socket = s;
//        ois = new ObjectInputStream(socket.getInputStream());
//        oos = new ObjectOutputStream(socket.getOutputStream());
//
//        // Start a thread for continuous listening
//        new Thread(this::listenToServer).start();
//
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
    portbtn.addActionListener(e -> {
        int choice = comboBox1.getSelectedIndex();
        selectedPort = SerialPort.getCommPort(SerialPort.getCommPorts()[choice - 1].getSystemPortName());

// faire avec un thread
        baudRate = 9600;
        System.out.println("Selected : " + choice);
        System.out.println("\nPre-setting RTS: " + (selectedPort.setRTS() ? "Success" : "Failure"));
                if (selectedPort.openPort()) {
                    new Thread(this::data).start();
                } else {
                    System.out.println("Error: Unable to open the serial port.");
                }
    });
}


private void data () {
    selectedPort.setComPortParameters(baudRate, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
    selectedPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    System.out.println("Port is open: " + selectedPort.getDescriptivePortName());

    input = new BufferedReader(new InputStreamReader(selectedPort.getInputStream()));
    String line;
    while (true) {
        try {
            if (!((line = input.readLine()) != null)) break;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        result.setText(line);
        System.out.println("Received from arduino : " + line);
        comboBox1.disable();
    }
}

//        private void listenToServer(){
//            try {
//                boolean listening = true;
//                while (listening) {
//                    received = (Message) ois.readObject();
//                    data = received.getMessage();
//                    valueLabel.setText(data);
//                    time = received.getTime();
//                    timeLabel.setText(time);
//                    timestampLabel.setText("connected a " + HOST + " on port " + socket.getLocalPort());
//                    System.out.println("Received from Server: " + received);
//                }
//            } catch (SocketException e) {
//                System.out.println("Server has closed the connection.");
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }


//    public void  lireFichier() throws IOException {
//        Desktop.getDesktop().open(new File("C:/Users/steve/IdeaProjects/Java2324/src/ExamenJava3"));
//    }
//    private void writefile(){
//        try {
//            namei++;
//            String fileName = "C:/Users/steve/IdeaProjects/Java2324/src/ExamenJava3/rec"+namei+".csv";
//            File csvFile = new File(fileName);
//            try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile))) {
//                System.out.println("RECButton clicked!");
//                while (isRecording) {
//                    if (received != null) {
//                        csvWriter.write(received.getMessage() + "," + received.getTime() + "\n");
//                        csvWriter.flush();
//                    }
//                    Thread.sleep(1000);
//                }
//            }
//        } catch (IOException | InterruptedException ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    public static void main (String[]args) throws IOException {

        SerialPort[] ports = SerialPort.getCommPorts();
        Client c = new Client();

        JFrame frame = new JFrame("Client");
        frame.setContentPane(c.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);


        c.comboBox1.addItem("Select a port");
        for (SerialPort listport : ports) {
            c.comboBox1.addItem(listport.getDescriptivePortName());
        }

    }
}