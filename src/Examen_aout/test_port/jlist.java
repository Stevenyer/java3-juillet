package Examen_aout.test_port;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class jlist {

    private JComboBox<String> comboBox1;
    private JPanel portlist;
    private JButton portbtn;
    private  JLabel result;
    private static String info;
    private static BufferedReader input = null;
    private static int baudRate;

    public jlist(){
        portbtn.addActionListener(e -> {

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
                comboBox1.disable();
                String line;
//                while (true) {
//                    try {
//                        if (!((line = input.readLine()) != null)) break;
//                    } catch (IOException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                    System.out.println("Received from arduino : " + line);
//                    //result.setText("Received from arduino : " + line);
//                }

            } else {
                System.out.println("Error: Unable to open the serial port.");
           }
        });
    }



    public static void main(String[] args) {
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