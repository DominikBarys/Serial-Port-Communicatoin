import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SerialCommunitation extends JFrame{

    SerialPort serialPort1;

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

    public SerialCommunitation(){
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

                    if(serialPort1.isOpen()){
                        JOptionPane.showMessageDialog(mainPanel, serialPort1.getDescriptivePortName() + " -- Success to OPEN!");
                        comPort.setEnabled(true);
                        comStatus.setValue(0);
                        open.setEnabled(true);
                        close.setEnabled(false);
                        send.setEnabled(false);
                    }else{
                        JOptionPane.showMessageDialog(mainPanel, serialPort1.getDescriptivePortName() + " -- Failed to OPEN!");
                    }

                }catch (ArrayIndexOutOfBoundsException a){

                }catch (Exception b){

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
}
