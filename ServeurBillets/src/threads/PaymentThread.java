/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import paymessage.PayMessage;
import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import interfaces.ServerConsole;
import interfaces.TasksSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import paymessage.PayResponseMessage;

/**
 *
 * @author Sadik
 */
public class PaymentThread extends Thread
{
    private int port;
    private ServerSocket SSocket = null;
    private ServerConsole app;
    private Runnable currentTask;
    private TasksSource tasks;
    private Socket CSocket = null;
    private int nbrClient;
    private boolean fin;
    private int waitingTasks;
    private BeanBDAccess bd; 
    private Configuration configuration;
    private PublicKey clientPK;
    private PrivateKey paymentPrK;
    public PaymentThread(int p,ServerConsole sc,BeanBDAccess b)
    {
        port = p; 
        app = sc;
        configuration = new Configuration();
        nbrClient = Integer.parseInt((configuration.getPropertie("POOL_NUMBER")));
        waitingTasks = 0;
        bd = b;
        getKeys();
    }
    private void getKeys()
    {
        try 
        {
            String keystorePassword = configuration.getPropertie("KEYSTORE_PASS");
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(new FileInputStream("../Resources/ServerPayment/ServerPayment.keystore"),keystorePassword.toCharArray());
            paymentPrK = (PrivateKey) ks.getKey("serverpaymentkey", keystorePassword.toCharArray());
            X509Certificate cert = (X509Certificate) (ks.getCertificate("clientcertificat"));
            clientPK = cert.getPublicKey();
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(PaymentThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException ex) 
        {
            Logger.getLogger(PaymentThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run()
    {
        try
        {
            SSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            System.err.println("Erreur lors de la création de la ServerSocket : " + e.getMessage()); 
            System.exit(1);
        }
        CSocket = null;
        while (!isInterrupted())
        {
            try
            {
                app.trace("Serveur#Serveur Payment en attente");
                CSocket = SSocket.accept();
                app.trace(CSocket.getRemoteSocketAddress().toString()+"#Client connecté");
            } 
            catch (IOException ex) 
            {
                System.err.println("Erreur lors du accept" + ex.getMessage());
                System.exit(1);
            }
            Thread th = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try 
                    {
                        ObjectOutputStream oos = new ObjectOutputStream(CSocket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(CSocket.getInputStream());
                        
                        PayMessage message = (PayMessage)ois.readObject();
                        if(message.getType() == PayMessage.REQUEST_PAYEMENT)
                        {
                            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                            cipher.init(Cipher.DECRYPT_MODE, paymentPrK);
                            String carte = new String(cipher.doFinal(message.getCarte()));
                            String nom = new String(cipher.doFinal(message.getNom()));
                            byte[] totalBytes = cipher.doFinal(message.getTotal());
                            byte[] signatureRecu = message.getSignature();
                            
                            ByteArrayInputStream bais = new ByteArrayInputStream(totalBytes);
                            DataInputStream dis = new DataInputStream(bais);
                            int total = dis.readInt();
                            Signature sign = Signature.getInstance("SHA1withRSA","BC"); 
                            sign.initVerify(clientPK);
                            sign.update(carte.getBytes());
                            sign.update(nom.getBytes());
                            sign.update(totalBytes);
                            
                            if(sign.verify(signatureRecu))
                            {
                                Random rnd = new Random();
                                int r = rnd.nextInt(10-1+1)+1;//1 à 10 compris;
                                if(r<11)
                                {
                                    //Refus
                                    oos.writeObject(new PayResponseMessage(PayResponseMessage.FAILED));
                                }
                                else
                                {
                                    //payement accepté
                                    oos.writeObject(new PayResponseMessage(PayResponseMessage.SUCCESS));
                                }
                            }
                            else
                            {
                                oos.writeObject(new PayResponseMessage(PayResponseMessage.FAILED));
                            }
                        }
                        CSocket.close();
                        return;
                    } 
                    catch (IOException | ClassNotFoundException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | SignatureException ex) 
                    {
                        Logger.getLogger(PaymentThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    try {
                        CSocket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(PaymentThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            });
            th.start();
        }
    }
}

