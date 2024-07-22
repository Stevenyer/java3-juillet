package Examen_aout;

import ExamenJava3_janvier_1.Client;
import ExamenJava3_janvier_1.Message;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class jlist {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 6000;
    private JComboBox<String> comboBox1;
    private JPanel portlist;
    private JButton portbtn;
    private  JLabel result;
    private static String info;
    private static BufferedReader input = null;
    private static int baudRate;
//    private Socket socket;
//    private ObjectOutputStream oos;
//    private ObjectInputStream ois;
//    private static Message received;
//    private static String data;
//    private static String time;
//    private BufferedWriter csvWriter;



    public jlist(){

        portbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice =  comboBox1.getSelectedIndex();
                SerialPort selectedPort = SerialPort.getCommPort(SerialPort.getCommPorts()[choice-1].getSystemPortName());
// faire avec un thread
                baudRate = 9600;
                System.out.println("Selected : " + choice);
                System.out.println("\nPre-setting RTS: " + (selectedPort.setRTS() ? "Success" : "Failure"));

                if (selectedPort.openPort()) {
                    selectedPort.setComPortParameters(baudRate, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                    selectedPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                    input = new BufferedReader(new InputStreamReader(selectedPort.getInputStream()));

                    String line;
                    while (true) {
                        try {
                            if (!((line = input.readLine()) != null)) break;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println("Received from arduino : " + line);
                        comboBox1.disable();
                    }

                } else {
                    System.out.println("Error: Unable to open the serial port.");
               }
            }
        });
    }

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
//    private void listenToServer() {
//        try {
//            boolean listening = true;
//            while (listening) {
//                received = (Message) ois.readObject();
//                data = received.getMessage();
//                valueLabel.setText(data);
//                time = received.getTime();
//                timeLabel.setText(time);
//                timestampLabel.setText("connected a "+HOST+" on port "+ socket.getLocalPort());
//                System.out.println("Received from Server: " + received);
//            }
//        } catch (SocketException e) {
//            System.out.println("Server has closed the connection.");
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) throws IOException {

//        Socket socket = new Socket(HOST, PORT);
//        Client client = new Client(socket);
        SerialPort[] ports = SerialPort.getCommPorts();
        jlist j = new jlist();

        JFrame frame = new JFrame("Port List");
        frame.setContentPane(j.portlist);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        // Adds the ports to the comboBox

        j.comboBox1.addItem("Select a port");
        for (SerialPort port : ports){
            j.comboBox1.addItem(port.getDescriptivePortName());
    }

    }
}