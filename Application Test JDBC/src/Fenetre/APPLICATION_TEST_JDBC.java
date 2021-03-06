/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Fenetre;

import database.utilities.BeanBDAccess;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Sadik
 */
public class APPLICATION_TEST_JDBC extends javax.swing.JFrame {

    /**
     * Creates new form AccessBD
     */
    private String choix;
    private boolean isMysql = true;
    private BeanBDAccess bd;
    public APPLICATION_TEST_JDBC() {
        initComponents();
        String tchoix[] ={"Mysql - bd airport","Oracle BD_JOURNALDEBORD"};
        choix = (String) JOptionPane.showInputDialog(this, "Choix de la BD","Choix BD",JOptionPane.QUESTION_MESSAGE,null,tchoix,tchoix[0]);
        if(choix==null)
            System.exit(0);
        if(choix == "Mysql - bd airport")
        {
            bd = new BeanBDAccess("MYSQL","bd_airport","root","sadikano");
        }
        else
        {
            bd = new BeanBDAccess("ORACLE","shirro","shirro","sadikano");
            tableCMB.removeAllItems();
            tableCMB.addItem("Activites");
            tableCMB.addItem("Intervenants");            
            isMysql= false;
        }
        bd.connectDB();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        requeteTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultatJT = new javax.swing.JTable();
        executeJB = new javax.swing.JButton();
        tableCMB = new javax.swing.JComboBox<>();
        CountCB = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Requête SQL : ");

        resultatJT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jScrollPane1.setViewportView(resultatJT);

        executeJB.setText("Executer");
        executeJB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeJBActionPerformed(evt);
            }
        });

        tableCMB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vols", "Billets", "Passager", "Bagages", "Avion", "Agents" }));

        CountCB.setSelected(true);
        CountCB.setText("Count");

        jButton1.setText("Executer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(requeteTF, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addComponent(executeJB))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tableCMB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CountCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(requeteTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(executeJB))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tableCMB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CountCB)
                    .addComponent(jButton1)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void executeJBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeJBActionPerformed
        executeRequest(requeteTF.getText());
    }//GEN-LAST:event_executeJBActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String request = "select";
        String table = (String)tableCMB.getSelectedItem();
        if(CountCB.isSelected()) request+=" count(*) as Nombre";
        else request+=" *";
        request+=" from "+table;
        executeRequest(request);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        bd.Close();
    }//GEN-LAST:event_formWindowClosing

    private void executeRequest(String request)
    {
        try 
        {
            
            String delimiter = " ";
            String[] TypeRequete = request.split(delimiter);
            System.out.println("Type de requête : " + TypeRequete[0]);
            int positiontable = 0;
            if(TypeRequete[0].toString().toUpperCase().equals("SELECT"))
            {
                for(int i = 0; i < TypeRequete.length ; i++)
                {
                    if(TypeRequete[i].toUpperCase().equals("FROM") == true)
                    {
                        positiontable = i + 1;
                        i = TypeRequete.length;
                    }
                }
                String NameOfTable = TypeRequete[positiontable].substring(0,TypeRequete[positiontable].length());
                String lock = isMysql ? "LOCK TABLE " + NameOfTable + " WRITE":"SET TRANSACTION READ ONLY";
                String unlock = isMysql ? "UNLOCK TABLE":"commit"; 
                bd.executeQuery(lock);
                ResultSet rs = bd.executeQuery(request);
                bd.executeQuery(unlock);
                int columns = rs.getMetaData().getColumnCount();
                Vector<String> columnNames = new Vector<String>();
                for (int i = 1; i <= columns; i++)
                    columnNames.add(rs.getMetaData().getColumnName(i));
                Vector<Vector<Object>> tuples = new Vector<Vector<Object>>();
                while(rs.next())
                {
                    Vector<Object> tuple = new Vector<Object>();
                    for (int i = 1; i <= columns; i++) {
                        tuple.add(rs.getObject(i));
                    }
                    tuples.add(tuple);
                }
                DefaultTableModel dtm = new DefaultTableModel(tuples,columnNames);
                resultatJT.setModel(dtm);
                rs.close();
            }
            else
            {
                if(TypeRequete[0].toUpperCase().equals("UPDATE") == true )
                {
                    String lock = isMysql ? "LOCK TABLE " + TypeRequete[1] + " WRITE":"SET TRANSACTION READ WRITE";
                    String unlock = isMysql ? "UNLOCK TABLE":"commit"; 
                    bd.executeQuery(lock);
                    bd.insertQuery(request);
                    bd.executeQuery(unlock);
                    int position = 0;
                    JOptionPane jop = new JOptionPane();
                    int option = jop.showConfirmDialog(null, "Voulez-vous afficher le contenu de la table: " + TypeRequete[position] + " ?", "QUESTION", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if(option == JOptionPane.YES_OPTION)
                    {
                        ResultSet rs = null;
                        String Query = "SELECT * FROM " +TypeRequete[1] + "";
                        bd.executeQuery(lock);
                        rs = bd.executeQuery(Query);
                        bd.executeQuery(unlock);
                        int columns = rs.getMetaData().getColumnCount();
                        Vector<String> columnNames = new Vector<String>();
                        for (int i = 1; i <= columns; i++)
                            columnNames.add(rs.getMetaData().getColumnName(i));
                        Vector<Vector<Object>> tuples = new Vector<Vector<Object>>();
                        while(rs.next())
                        {
                            Vector<Object> tuple = new Vector<Object>();
                            for (int i = 1; i <= columns; i++) {
                                tuple.add(rs.getObject(i));
                            }
                            tuples.add(tuple);
                        }
                        DefaultTableModel dtm = new DefaultTableModel(tuples,columnNames);
                        resultatJT.setModel(dtm);
                        rs.close();
                    }
                }
                else if(TypeRequete[0].toUpperCase().equals("INSERT")  || TypeRequete[0].toUpperCase().equals("DELETE") == true)
                {
                    String lock = isMysql ? "LOCK TABLE " + TypeRequete[2] + " WRITE":"SET TRANSACTION READ WRITE";
                    String unlock = isMysql ? "UNLOCK TABLE":"commit"; 
                    bd.executeQuery(lock);
                    bd.insertQuery(request);
                    bd.executeQuery(unlock);
                    int position = 0;
                    JOptionPane jop = new JOptionPane();
                    int option = jop.showConfirmDialog(null, "Voulez-vous afficher le contenu de la table: " + TypeRequete[position] + " ?", "QUESTION", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if(option == JOptionPane.YES_OPTION)
                    {
                        ResultSet rs = null;
                        String Query = "SELECT * FROM " +TypeRequete[2] + "";
                        bd.executeQuery(lock);
                        rs = bd.executeQuery(Query);
                        bd.executeQuery(unlock);
                        int columns = rs.getMetaData().getColumnCount();
                        Vector<String> columnNames = new Vector<String>();
                        for (int i = 1; i <= columns; i++)
                            columnNames.add(rs.getMetaData().getColumnName(i));
                        Vector<Vector<Object>> tuples = new Vector<Vector<Object>>();
                        while(rs.next())
                        {
                            Vector<Object> tuple = new Vector<Object>();
                            for (int i = 1; i <= columns; i++) {
                                tuple.add(rs.getObject(i));
                            }
                            tuples.add(tuple);
                        }
                        DefaultTableModel dtm = new DefaultTableModel(tuples,columnNames);
                        resultatJT.setModel(dtm);
                        rs.close();
                    }
                }
                
                
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(APPLICATION_TEST_JDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(APPLICATION_TEST_JDBC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(APPLICATION_TEST_JDBC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(APPLICATION_TEST_JDBC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(APPLICATION_TEST_JDBC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new APPLICATION_TEST_JDBC().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CountCB;
    private javax.swing.JButton executeJB;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField requeteTF;
    private javax.swing.JTable resultatJT;
    private javax.swing.JComboBox<String> tableCMB;
    // End of variables declaration//GEN-END:variables
}
