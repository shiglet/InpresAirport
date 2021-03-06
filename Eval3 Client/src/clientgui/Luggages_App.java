/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import ConfigurationFile.Configuration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.TableModelEvent;
import models.LuggageModel;
import models.TableItemModel;
import request.LUGAPRequest;
import response.LUGAPResponse;

/**
 *
 * @author Sadik
 */
public class Luggages_App extends javax.swing.JFrame {

    /**
     * Creates new form Luggages_App
     */
    private Socket socket;
    private Configuration configuration;
    private String trameSep;
    private String IP;
    private int port;
    private ObjectOutputStream oos ;
    private ObjectInputStream ois ;
    private Bagages  b ;
    public Luggages_App() {
        initComponents();
        configuration = new Configuration();
        trameSep = configuration.getPropertie("TRAME_SEPARATOR");
        IP = configuration.getPropertie("Server_IP");
        port = Integer.parseInt(configuration.getPropertie("PORT_BAGGAGES"));
        login();
    }
    
    
    public void tableChanged(TableModelEvent e) 
    {
        System.out.println("Change");
        TableItemModel tim = (TableItemModel) e.getSource();
        LuggageModel l = tim.getLuggageAt(e.getLastRow());
        LUGAPRequest req = new LUGAPRequest(LUGAPRequest.UPDATE_LUGGAGE,l);
        try 
        {
            oos.reset();//cause of sending always the same object reference
            oos.writeObject(req);
            LUGAPResponse rep = (LUGAPResponse) ois.readObject();
            b.initJTable(rep.getvLuggages());
        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public void exit()
    {
        System.exit(0);
    }
    private void login()
    {
        boolean authenticated = false;
        
        Connexion con = new Connexion(this,true);
        String login, password;
        try 
        {
            
            while(!authenticated)
            {
                con.setVisible(true);
                if(!con.isDisplayable()) //if disposed (cross click)
                    System.exit(0);
                login = con.getLogin();
                password = con.getPassword();
                System.out.println(password);
                socket = new Socket(IP,port);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                
            
                long time = (new Date()).getTime();
                double r = Math.random();
                
                byte[] msgD = buildDigest(time,r,password,login);
                LUGAPRequest req = new LUGAPRequest(LUGAPRequest.REQUEST_LOGIN,login+trameSep+time+trameSep+r,msgD);
                oos.writeObject(req);
                LUGAPResponse rep = (LUGAPResponse)ois.readObject();
                if(rep.getCode() == LUGAPResponse.LOGIN_SUCCESS)
                    break;
                JOptionPane.showMessageDialog(rootPane, rep.message);
                oos.close();
                ois.close();
                socket.close();
            }

        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        getFly();
    }
     private void getFly() 
    {
        try 
        {
            LUGAPRequest req = new LUGAPRequest(LUGAPRequest.REQUEST_FLYLIST);
            oos.writeObject(req);
            LUGAPResponse rep = (LUGAPResponse) ois.readObject();
            if(rep.getCode() != LUGAPResponse.FLYLIST_SUCCESS)
            {
                JOptionPane.showMessageDialog(rootPane, "Erreur lors de la récupération de la liste des vols");
                return;
            }
            if(rep.vData.size()<1)
            {
                JOptionPane.showMessageDialog(rootPane, "Pas de vol pour aujourd'hui");
                return;
            }
            flyJL.setListData(rep.vData);
        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        flyJL = new javax.swing.JList<>();

        jScrollPane2.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Application baggages");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/baggage-drop-off_200x200.jpg"))); // NOI18N

        flyJL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                flyJLMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(flyJL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(262, 262, 262)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(53, 53, 53)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void flyJLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_flyJLMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() < 2) 
            return;
        
        // Double-click detected
        try 
        {
            int index = flyJL.locationToIndex(evt.getPoint());
            ListModel<String> dlm = flyJL.getModel();
            String fly = dlm.getElementAt(index);
            String[] tokens = fly.split(" ");
            LUGAPRequest req = new LUGAPRequest(LUGAPRequest.REQUEST_LUGGAGES,tokens[1]);
            oos.writeObject(req);
            LUGAPResponse rep = (LUGAPResponse) ois.readObject();
            Vector<LuggageModel> vLuggage = rep.getvLuggages();
            b = new Bagages(this,true,vLuggage);
            b.setVisible(true);
            logout();
            login();
        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_flyJLMouseClicked
    private void logout() {
        if(socket==null || socket.isClosed())
                return;
        try
        {
            oos.writeObject(new LUGAPRequest(LUGAPRequest.REQUEST_LOGOUT));
            LUGAPResponse rep = (LUGAPResponse) ois.readObject();
            if(rep.getCode() == LUGAPResponse.LOGOUT_SUCCESS)
                System.out.println("Deconnexion réussie !");
            oos.close();
            ois.close();
            socket.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        logout();
    }//GEN-LAST:event_formWindowClosing
    
    public byte[] buildDigest(long time,double r,String password,String login)
    {
        byte[] msgD = null;
        try 
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1", "BC");
            md.update(login.getBytes());
            md.update(password.getBytes());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream bdos = new DataOutputStream(baos);
            bdos.writeLong(time);
            bdos.writeDouble(r);
            
            md.update(baos.toByteArray());
            msgD = md.digest();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            Logger.getLogger(Luggages_App.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msgD;
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
            java.util.logging.Logger.getLogger(Luggages_App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Luggages_App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Luggages_App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Luggages_App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Luggages_App().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> flyJL;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    

}
