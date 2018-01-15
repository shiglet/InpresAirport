/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import ConfigurationFile.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import message.FlyMessage;
import message.HandshakeMessage;
import message.LoginMessage;
import message.SimpleMessage;
import request.TICKMAPRequest;
import response.TICKMAPResponse;

/**
 *
 * @author Sadik
 */
public class Application_Billets extends javax.swing.JFrame {

    /**
     * Creates new form Application_Billets
     */
    private Socket socket;
    private Configuration configuration;
    private String IP;
    private int port;
    private ObjectOutputStream oos ;
    private ObjectInputStream ois ;
    private SecretKey authenticationK;
    private SecretKey cipherK;
    private PrivateKey clientPrK;
    private PublicKey serverPK;
    public Application_Billets() 
    {
        initComponents();
        configuration = new Configuration();
        IP = configuration.getPropertie("BILLETS_IP");
        port = Integer.parseInt(configuration.getPropertie("PORT_BILLETS"));
        getKeys();
        login();
    }

    private void getKeys()
    {
        try 
        {
            String keystorePassword = configuration.getPropertie("KEYSTORE_PASS");
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(new FileInputStream("../Resources/ClientCrypto/InpresAirportClient.keystore"),keystorePassword.toCharArray());
            clientPrK = (PrivateKey) ks.getKey("client", keystorePassword.toCharArray());
            X509Certificate cert = (X509Certificate) (ks.getCertificate("serveurcertificat"));
            serverPK = cert.getPublicKey();
        }
        catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException ex) 
        {
            Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
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
                System.out.println("l = "+login+" r = "+r+" time = "+time+"");
                TICKMAPRequest req = new TICKMAPRequest(TICKMAPRequest.REQUEST_LOGIN,new LoginMessage(r,msgD,login,time));
                oos.writeObject(req);
                TICKMAPResponse rep = (TICKMAPResponse)ois.readObject();
                if(rep.getCode() == TICKMAPResponse.SUCCESS)
                    break;
                JOptionPane.showMessageDialog(rootPane, ((SimpleMessage)rep.getMessage()).getMessage());
                oos.close();
                ois.close();
                socket.close();
            }

        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        handshake();
    }
    
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
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msgD;
    }
    
    private void logout() 
    {
        if(socket==null || socket.isClosed())
                return;
        try
        {
            oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_LOGOUT));
            TICKMAPResponse rep = (TICKMAPResponse) ois.readObject();
            if(rep.getCode() == TICKMAPResponse.SUCCESS)
                System.out.println("Deconnexion réussie !");
            oos.close();
            ois.close();
            socket.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handshake() 
    {
        try 
        {
            oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_HANDSHAKE));
            TICKMAPResponse rep = (TICKMAPResponse) ois.readObject();
            if(rep.isSuccess())
            {
                //Encrypted secret keys
                byte[] a = ((HandshakeMessage)rep.getMessage()).getAuthenticationK();
                byte[] c = ((HandshakeMessage)rep.getMessage()).getCipherK();
                //Get a Cipher object
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                cipher.init(Cipher.DECRYPT_MODE, clientPrK);
                //Decrypt the keys
                byte[] authKeyBytes = cipher.doFinal(a);
                byte[] cipherKeyBytes = cipher.doFinal(c);
                //Convert Byte keys  into SecretKey
                authenticationK = new SecretKeySpec(authKeyBytes, 0, authKeyBytes.length, "DES");
                cipherK = new SecretKeySpec(cipherKeyBytes, 0, cipherKeyBytes.length, "DES");
                rep = (TICKMAPResponse)ois.readObject();
                System.out.println("Nombre d'élements : "+((FlyMessage)rep.getMessage()).getFlies().size());
            }
            else
            {
                logout();
            }
        }
        catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) 
        {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 751, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 507, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(Application_Billets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Application_Billets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Application_Billets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Application_Billets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Application_Billets().setVisible(true);
            }
        });
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
