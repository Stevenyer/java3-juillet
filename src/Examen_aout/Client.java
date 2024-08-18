package Examen_aout;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Client {
    private static String data;
    private static SerialPort selectedPort;
    String[] entetes = {"Data", "Time"};
    private JComboBox<String> comboBox1;
    private JButton portbtn;
    private JPanel panel1;
    private JLabel result, valueLabel, timeLabel, timestampLabel;
    private JButton recbtn;
    private JTable table1;
    private JButton erasebtn;
    private JButton savebtn;
    private JScrollPane scroll;
    private BufferedReader input;
    private int baudRate = 9600;
    private int namei;
    private boolean isRecording = false;

    public Client() {
        table1.setModel(new MyTableModel());


        portbtn.addActionListener(e -> {

            int choice = comboBox1.getSelectedIndex();
            selectedPort = SerialPort.getCommPort(SerialPort.getCommPorts()[choice - 1].getSystemPortName());
            System.out.println("Selected : " + choice);
            System.out.println("\nPre-setting RTS: " + (selectedPort.setRTS() ? "Success" : "Failure"));
            if (selectedPort.openPort()) {
                comboBox1.setEnabled(false);
                portbtn.setEnabled(false);
                recbtn.setEnabled(true);
                new Thread(this::data).start();
            } else {
                System.out.println("Error: Unable to open the serial port.");
            }
        });
        recbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("RECButton clicked!");

                if (!isRecording) {
                    isRecording = true;
                    erasebtn.setEnabled(false);
                    savebtn.setEnabled(false);
                    new Thread(Client.this::record).start();
                } else {
                    isRecording = false;
                    erasebtn.setEnabled(true);
                    savebtn.setEnabled(true);
                }

            }
        });
        savebtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRecording) {
                    new Thread(Client.this::writefile).start();
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez arrêter l'enregistrement avant de sauvegarder le fichier.");
                }
            }
        });
        erasebtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                erase();
            }
        });
    }

    private static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static void main(String[] args) throws IOException {

        SerialPort[] ports = SerialPort.getCommPorts();
        Client c = new Client();


        JFrame frame = new JFrame("Client");
        frame.setContentPane(c.panel1);
        frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        c.comboBox1.addItem("Select a port");
        for (SerialPort listport : ports) {
            c.comboBox1.addItem(listport.getDescriptivePortName());
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (selectedPort != null) {
                    selectedPort.closePort();
                    System.out.println("Port is closed: " + selectedPort.getDescriptivePortName());
                    System.exit(0);
                }
            }
        });
    }

    private void createUIComponents() {
        scroll = new JScrollPane(table1);
        scroll.setAutoscrolls(true);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void data() {
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
            data = line;
            System.out.println("Received from arduino : " + line);
        }
    }

    private void erase() {
        if (!isRecording && table1.getRowCount() > 0) {
            int response = JOptionPane.showConfirmDialog(null, "Voulez-vous vraiment effacer la table ?", "Effacer la table", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ((MyTableModel) table1.getModel()).data.clear();
                table1.updateUI();
                JOptionPane.showMessageDialog(null, "La table a été effacée.");
            } else if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "La table n'a pas été effacée.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "La table est vide.");
        }
    }

    private void record() {
        while (isRecording) {
            savebtn.setEnabled(false);
            erasebtn.setEnabled(false);
            if (data != null) {
                final String dataFinal = data;
                final String time = getCurrentTime();
                System.out.println(dataFinal + "," + time + "\n");
                SwingUtilities.invokeLater(() -> {
                    ((MyTableModel) table1.getModel()).addRow(new Object[]{dataFinal, time});
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void writefile() {
        namei++;
        String fileName = "rec" + namei + ".csv";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le fichier");
        fileChooser.setFileFilter(new FileNameExtensionFilter(fileName, "csv"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            // Ajoute une extension .txt si elle est absente
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try {
                try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(filePath))) {
                    csvWriter.write("Data,Time\n");
                    for (int i = 0; i < table1.getRowCount(); i++) {
                        csvWriter.write(table1.getValueAt(i, 0) + "," + table1.getValueAt(i, 1) + "\n");
                    }
                    csvWriter.flush();
                    csvWriter.close();
                    System.out.println("File saved successfully!");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class MyTableModel extends AbstractTableModel {
        private String[] columnNames = {"Data", "Time"};
        private List<Object[]> data = new ArrayList<>();

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex)[columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        public void addRow(Object[] rowData) {
            data.add(rowData);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }
    }
}