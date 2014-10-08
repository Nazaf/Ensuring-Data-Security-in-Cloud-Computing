package Server;

import javax.swing.*;
import javax.swing.SwingConstants;

public class TPA extends javax.swing.JFrame {

    /**
     * Creates new form TPA
     */
    public TPA() {
        initComponents();
        this.setTitle("Server - TPA");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpaTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(640, 480));
        setResizable(false);

		JPanel jpan = new JPanel();
		jpan.setBackground(new java.awt.Color(0, 116, 232));
	    jpan.setBorder(new javax.swing.border.EtchedBorder());
		jpan.setLayout(null);
        
        tpaTextArea.setColumns(32);
        tpaTextArea.setEditable(false);
        tpaTextArea.setRows(8);
        jScrollPane1.setViewportView(tpaTextArea);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); 
        jLabel1.setText("Third Party Auditor");
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(125, 125, 125)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(233, 233, 233)
                        .addComponent(jLabel1)))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(149, 149, 149)
                .addComponent(jLabel1)
                .addGap(29, 29, 29)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
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

    public void setTextArea(String Text)
    {
        tpaTextArea.setText(tpaTextArea.getText()+Text);
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
            java.util.logging.Logger.getLogger(TPA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TPA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TPA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TPA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TPA().setVisible(true);
            }
        });
    }

    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JTextArea tpaTextArea;
}
