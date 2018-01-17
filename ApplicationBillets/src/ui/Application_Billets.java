/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import paymessage.PayMessage;
import ConfigurationFile.Configuration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import message.*;
import models.*;
import paymessage.PayResponseMessage;
import request.TICKMAPRequest;
import response.TICKMAPResponse;
import uimodels.TableItemModel;

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
    private PublicKey paymentPK;
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
            cert = (X509Certificate) (ks.getCertificate("paymentcertificat"));
            paymentPK = cert.getPublicKey();
            
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
                authenticationK = new SecretKeySpec(authKeyBytes, 0, authKeyBytes.length, "Rijndael");
                cipherK = new SecretKeySpec(cipherKeyBytes, 0, cipherKeyBytes.length, "Rijndael");
                rep = (TICKMAPResponse)ois.readObject();
                TableItemModel tableItemModel = new TableItemModel(((FlyMessage)rep.getMessage()).getFlies());
                fliesJT.setModel(tableItemModel);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        fliesJT = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        nbrPlaceJTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fliesJT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(fliesJT);

        jButton1.setText("Réserver");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        nbrPlaceJTF.setText("1");

        jLabel1.setText("Nombre de place :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 798, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nbrPlaceJTF, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(nbrPlaceJTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(nbrPlaceJTF.getText().length()<1)
            return;
        TableItemModel model = (TableItemModel)fliesJT.getModel();
        Fly f = model.getFlyAt(fliesJT.getSelectedRow());
        int nbrPlace = Integer.parseInt(nbrPlaceJTF.getText());
        Vector<Voyageur> vVoyageur = new Vector<Voyageur>();
        for(int i=0; i<nbrPlace;i++)
        {
            VoyageurConfirm vc = new VoyageurConfirm(this, true);
            vc.setVisible(true);
            vVoyageur.add(vc.voyageur);
        }
        
        try 
        {
            Cipher cipher = Cipher.getInstance("Rijndael/ECB/PKCS5Padding","BC");
            cipher.init(Cipher.ENCRYPT_MODE,cipherK);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(baos);
            o.writeObject(vVoyageur);
            
            byte[] vVoyageurBytes = baos.toByteArray();
            byte[] vVoyageurEncrypted = cipher.doFinal(vVoyageurBytes);
            
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(f.getIdVol());
            
            byte[] idVolBytes = baos.toByteArray();
            byte[] idVolEncrypted = cipher.doFinal(idVolBytes);
            
            oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_BOOKFLY,new BookMessage(idVolEncrypted,vVoyageurEncrypted)));
            TICKMAPResponse rep = (TICKMAPResponse)ois.readObject();
            BookFlyResponseMessage message = (BookFlyResponseMessage)rep.getMessage();
            cipher.init(Cipher.DECRYPT_MODE,cipherK);
            
            byte[] dataBytes = cipher.doFinal(message.getData());
            ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
            DataInputStream dis = new DataInputStream(bais);
            int numeroBillet = dis.readInt();
            int prixTotal = dis.readInt();
            
           
            System.out.println("Numero de Billet = "+numeroBillet+" PrixTotal = "+prixTotal);
            int result = JOptionPane.showConfirmDialog(rootPane, "Veuillez confirmer s'il vous plaît : Numéro de billet "+numeroBillet+" et le prix total : "+prixTotal+" €","Confirmation",JOptionPane.YES_OPTION);
            if(result == 0)//Yes
            {
                Mac hmac = Mac.getInstance("HMAC-MD5","BC");
                hmac.init(authenticationK);
                String m = "Client a bien confirmé";
                hmac.update(m.getBytes());
                byte[] hmacBytes = hmac.doFinal();
                oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_CONFIRMFLY,new ConfirmFlyMessage(hmacBytes, m)));
                rep = (TICKMAPResponse)ois.readObject();
                if(rep.getCode() == TICKMAPResponse.SUCCESS)
                {
                    Socket s = new Socket(configuration.getPropertie("IP_PAYMENT"),Integer.parseInt(configuration.getPropertie("PORT_PAY")));
                    ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                    cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                    cipher.init(Cipher.ENCRYPT_MODE, paymentPK);
                    
                    String carte = JOptionPane.showInputDialog(this,"Numéro de carte bancaire : ");
                    baos = new ByteArrayOutputStream();
                    
                    dos = new DataOutputStream(baos);
                    dos.writeInt(prixTotal);
                    byte[] carteEncrypted = cipher.doFinal(carte.getBytes());
                    byte[] nomEncrypted = cipher.doFinal(((Voyageur)vVoyageur.elementAt(0)).getNom().getBytes());
                    byte[] totalEncrypted = cipher.doFinal(baos.toByteArray());
                    Signature sign = Signature.getInstance("SHA1withRSA","BC"); 
                    sign.initSign(clientPrK);
                    sign.update(carte.getBytes());
                    sign.update(((Voyageur)vVoyageur.elementAt(0)).getNom().getBytes());
                    sign.update(baos.toByteArray());
                    byte[] signature = sign.sign();
                    os.writeObject(new PayMessage(PayMessage.REQUEST_PAYEMENT,carteEncrypted,nomEncrypted, totalEncrypted,signature));
                    PayResponseMessage msg = (PayResponseMessage)is.readObject();
                    s.close();
                    
                    cipher.init(Cipher.ENCRYPT_MODE,serverPK);
                    hmac = Mac.getInstance("HMAC-MD5","BC");
                    hmac.init(authenticationK);
                    hmac.update(carte.getBytes());
                    hmacBytes = hmac.doFinal();
                    if(msg.getType() == PayResponseMessage.SUCCESS)
                    {
                        
                        oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_CONFIRMPAY, new ConfirmPayMessage(ConfirmPayMessage.SUCCESS,hmacBytes,cipher.doFinal(carte.getBytes()))));
                        rep = (TICKMAPResponse) ois.readObject();
                        if(rep.getCode() == rep.SUCCESS)
                        {
                            JOptionPane.showMessageDialog(this,"Achat définitif effectué avec succès!");
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(this,"Erreur lors de l'achat!");
                        }
                    }
                    else
                    {
                        oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_CONFIRMPAY, new ConfirmPayMessage(ConfirmPayMessage.FAILED,hmacBytes,cipher.doFinal(carte.getBytes()))));
                        JOptionPane.showMessageDialog(this,"Erreur lors de l'achat!");
                    }
                }
            }
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException ex)
        {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(Application_Billets.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

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
    private javax.swing.JTable fliesJT;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nbrPlaceJTF;
    // End of variables declaration//GEN-END:variables
}
