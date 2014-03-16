import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class FileSelect extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonLoad;
    private JFileChooser fileSelect;
    private File selectedFile;
    public FileSelect() {
        setContentPane(contentPane);
        setModal(true);
        fileSelect = new JFileChooser(".");

        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLoad();
            }
        });

// call onLoad() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
        buttonOK.setText("Zip a file");
        buttonLoad.setText("Unzip a file");
// call onLoad() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLoad();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setVisible(true);
    }

    private void onOK() {
        int result = fileSelect.showOpenDialog(contentPane);
        if (result == JFileChooser.APPROVE_OPTION)
            selectedFile = fileSelect.getSelectedFile();
        if( SZip.zip(this))
            JOptionPane.showMessageDialog(contentPane,"Compression successfull!","Done",JOptionPane.INFORMATION_MESSAGE);
    }
    private void onLoad() {
        JFileChooser loadFileSelect = new JFileChooser(".");
        int result = loadFileSelect.showOpenDialog(contentPane);
        if (result == JFileChooser.APPROVE_OPTION)
            selectedFile = loadFileSelect.getSelectedFile();

        SZip.unzip(this);
    }

    private void onExit(){
        dispose();
    }
    public File getSelectedFile() {
        return selectedFile;
    }

    public static void main(String[] args) {
        FileSelect dialog = new FileSelect();
        dialog.pack();
        dialog.setVisible(true);
    }
}
