import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.OutputStream;
import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SerialCommunitation extends JFrame implements Runnable{

    Thread thread;
    long waitingForPing = 0L;
    boolean failedToPing = false;
    private final SerialCommunitation self;
    long start;
    SerialPort serialPort1;
    OutputStream outputStream1;
    String dataBuffer = "";

    private JPanel mainPanel;
    private JComboBox comPort;
    private JComboBox baudRate;
    private JComboBox stopBits;
    private JComboBox dataBits;
    private JComboBox patiryBits;
    private JProgressBar comStatus;
    private JPanel comSettings;
    private JButton open;
    private JButton close;
    private JTextArea dataToSend;
    private JButton send;
    private JComboBox endLine;
    private JTextField incomingData;
    private JButton ping;

    public SerialCommunitation(){
        self = this;
        initComponents();
        baudRate.setSelectedItem("9600");
        dataBits.setSelectedItem("8");
        stopBits.setSelectedItem("1");
        patiryBits.setSelectedItem("NO_PARITY");
        endLine.setSelectedItem("None");

        comPort.setEnabled(true);
        comStatus.setValue(0);
        open.setEnabled(true);
        close.setEnabled(false);
        send.setEnabled(false);
        comPort.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                comPort.removeAllItems();
                SerialPort[] portList = SerialPort.getCommPorts();
                for(SerialPort port : portList){
                    comPort.addItem(port.getSystemPortName());
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    SerialPort[] portLists = SerialPort.getCommPorts();
                    serialPort1 = portLists[comPort.getSelectedIndex()];
                    serialPort1.setBaudRate(Integer.parseInt(baudRate.getSelectedItem().toString()));
                    serialPort1.setNumDataBits(Integer.parseInt(dataBits.getSelectedItem().toString()));
                    serialPort1.setNumStopBits(Integer.parseInt(stopBits.getSelectedItem().toString()));
                    serialPort1.openPort();
                    outputStream1 = serialPort1.getOutputStream();
                    ping.setEnabled(true);

                    if(serialPort1.isOpen()){
                        JOptionPane.showMessageDialog(mainPanel, serialPort1.getDescriptivePortName() + " -- Success to OPEN!");
                        comPort.setEnabled(false);
                        comStatus.setValue(100);
                        open.setEnabled(false);
                        close.setEnabled(true);
                        send.setEnabled(true);

                        Serial_EventBasedReading(serialPort1);

                    }else{
                        JOptionPane.showMessageDialog(mainPanel, serialPort1.getDescriptivePortName() + " -- Failed to OPEN!");
                    }

                }catch (ArrayIndexOutOfBoundsException a){
                        JOptionPane.showMessageDialog(mainPanel, "Please choose COM PORT", "ERROR", ERROR_MESSAGE);
                }catch (Exception b){
                        JOptionPane.showMessageDialog(mainPanel, b, "ERROR", ERROR_MESSAGE);
                }
            }
        });
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(serialPort1.isOpen()){
                    serialPort1.closePort();
                    comPort.setEnabled(true);
                    comStatus.setValue(0);
                    open.setEnabled(true);
                    close.setEnabled(false);
                    send.setEnabled(false);
                }
            }
        });
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputStream1 = serialPort1.getOutputStream();
                String textAreaDataToSend = "";


                switch (endLine.getSelectedIndex()){
                    case 0:
                        textAreaDataToSend = dataToSend.getText(); //none
                        break;
                    case 1:
                        textAreaDataToSend = dataToSend.getText() + "\n"; //new line
                        break;
                    case 2:
                        textAreaDataToSend = dataToSend.getText() + "\r"; //carriage return
                        break;
                    case 3:
                        textAreaDataToSend = dataToSend.getText() + "\r\n"; // both
                        break;
                }

               try{
                    outputStream1.write(textAreaDataToSend.getBytes());
               }catch (IOException ioex){
                   JOptionPane.showMessageDialog(mainPanel, ioex.getMessage());
               }


            }
        });
        ping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                start = System.currentTimeMillis();

                dataBuffer = "";

                thread = new Thread(self);

                thread.start();

                System.out.println(dataBuffer);
                System.out.println("SIEMA 1");

                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                System.out.println("SIEMA 2");

                if(failedToPing){
                    long end = System.currentTimeMillis();
                    JOptionPane.showMessageDialog(mainPanel, "Ping failed\nTime elapsed: "
                            + (end - start) + " miliseconds");
                }else{
                    long end = System.currentTimeMillis();
                    JOptionPane.showMessageDialog(mainPanel, "Ping was sent\nTime elapsed: "
                            + (end - start) + " miliseconds" );
                }
            }
        });
    }

    public void Serial_EventBasedReading(SerialPort activePort){
        activePort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                byte[] newData = serialPortEvent.getReceivedData();
                for (int i = 0; i < newData.length; i++){
                    dataBuffer += (char)newData[i];
                    incomingData.setText(dataBuffer);
                }
            }
        });
    }

    public void initComponents(){
        setContentPane(mainPanel);
        setSize(800,600);
        setTitle("Serial Port Communication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    public static void main(String[] args) {
        new SerialCommunitation();
    }

    @Override
    public void run() {
        try{
            outputStream1.write(0x5);
            outputStream1.write("PING".getBytes());
            Thread.sleep(5);
            System.out.println("Data buffer 1: " + dataBuffer);
            while(!dataBuffer.equals("PONG")){
                waitingForPing = System.currentTimeMillis();
                if((waitingForPing - start) >= 3000){
                    failedToPing = true;
                    break;
                }
            }
        }catch (IOException ex){
            System.out.println("IOException ex");
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException ex");
            throw new RuntimeException(ex);
        }finally {
            System.out.println("Data buffer 2: " + dataBuffer);
        }
    }
}
