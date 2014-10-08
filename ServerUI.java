package Server;

import java.awt.Desktop;
import java.io.*;
import javax.swing.*;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class ServerUI extends javax.swing.JFrame {

public static class OnlyExt implements FilenameFilter
    {
        String ext;
        public OnlyExt(String ext)
        {
            this.ext = "." + ext;
        }
        public boolean accept(File dir, String name)
        {
            return name.endsWith(ext);
        }
    }
    
    public ServerUI() {
    	this.setTitle("Server");
        initComponents();
        setUserList();
    }

    public void setUserList()
    {       
        usersComboBox.addItem("admin");
        usersComboBox.addItem("student");
    }   
    
    @SuppressWarnings("unchecked")

    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        usersComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        filesComboBox = new javax.swing.JComboBox();
        openButton = new javax.swing.JButton();
        titleLabel1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

		ImageIcon ref = new ImageIcon("images/refresh.png");
		ImageIcon refDep = new ImageIcon("images/refreshDep.png");
		refreshButton = new javax.swing.JButton(ref);
		refreshButton.setPressedIcon(refDep);
		refreshButton.setSize(20,20);
		refreshButton.setBorder(null);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setOpaque(false);
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        setPreferredSize(new java.awt.Dimension(640, 480));
        setResizable(false);

		JPanel jpan = new JPanel();
		jpan.setBackground(new java.awt.Color(0, 116, 232));
	    jpan.setBorder(new javax.swing.border.EtchedBorder());
		jpan.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Users");

        usersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usersComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Files");

        filesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filesComboBoxActionPerformed(evt);
            }
        });

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 14));
        jLabel1.setText("Server");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(usersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(95, 95, 95)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(filesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(259, 259, 259)
                        .addComponent(jLabel1)
					.addGap(200,200,200)
					.addComponent(refreshButton)))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(jLabel1)
				.addGap(0,0,0)
				.addComponent(refreshButton)
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addComponent(openButton)
                .addContainerGap(164, Short.MAX_VALUE))
        );

		ImageIcon titlepic = new ImageIcon("images/title.JPG");
        titleLabel1.setIcon(titlepic);
		jpan.add(titleLabel1);
		titleLabel1.setBounds(0,0,640,120);

		JLabel imageLabel = new JLabel();
        ImageIcon ii = new ImageIcon("images/bg.JPG");
        imageLabel.setIcon(ii);
        jpan.add(imageLabel);
		imageLabel.setBounds(0,0,640,480);

		
		getContentPane().add(jpan);
		jpan.setBounds(0,0,640,480);

        pack();
    }

    private void usersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        filesComboBox.removeAllItems();
        
        String Username = usersComboBox.getSelectedItem().toString();
        
        File userDirectory = new File(Username);
        if(!userDirectory.exists())
            return;
        File Files[] = userDirectory.listFiles();

        for(File afile: Files){
        	if(afile.isDirectory()) continue;
            filesComboBox.addItem(afile.getName());
        }
    }

    private void filesComboBoxActionPerformed(java.awt.event.ActionEvent evt)
	{
    }

	 private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try
        {
			filesComboBox.removeAllItems();
        
        String Username = usersComboBox.getSelectedItem().toString();
        
        File userDirectory = new File(Username);
        if(!userDirectory.exists())
            return;
        File Files[] = userDirectory.listFiles();

        for(File afile: Files){
        	if(afile.isDirectory()) continue;
            filesComboBox.addItem(afile.getName());
		}
       }
        catch(Exception ex)
        {
        }
    }

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	 String filename=null;
        try
        {
            filename = usersComboBox.getSelectedItem().toString() + "\\" + filesComboBox.getSelectedItem().toString();
			//Open file using the OS associated program for the file type
			Desktop.getDesktop().open(new File(filename));
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        	JOptionPane.showMessageDialog(this,"Couldn't open file " + filename+"!\n Error:" + ex.getMessage());
        }
    }

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
       
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ServerUI().setVisible(true);
            }
        });
    }
   
    private javax.swing.JComboBox filesComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton openButton;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JComboBox usersComboBox;
    private javax.swing.JButton refreshButton;
   
}
