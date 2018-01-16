/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import interfaces.Message;
import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import interfaces.Request;
import interfaces.ServerConsole;
import java.applet.Applet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import message.*;
import models.*;
import response.TICKMAPResponse;

/**
 *
 * @author Sadik
 */
public class TICKMAPRequest implements Request, Serializable
{
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_LOGOUT = 1;
    public static final int REQUEST_HANDSHAKE = 2;
    public static final int REQUEST_BOOKFLY = 3;
    public static final int REQUEST_CONFIRMFLY = 4;
    public static final int REQUEST_CONFIRMPAY = 5;
    
    private int type;

    public Message getMessage() {
        return message;
    }
    private Message message;
    
    public TICKMAPRequest(int type,Message message)
    {
        this.type = type;
        this.message = message;
    }
    
    public TICKMAPRequest(int type)
    {
        this.type = type;
    }
    
    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs, ObjectOutputStream oos, ObjectInputStream ois, BeanBDAccess beanBD) 
    {
        return new Runnable()
        {
            private String state = "NON_AUTHENTICATED";
            private BeanBDAccess bd = beanBD;
            private TICKMAPResponse rep;
            private TICKMAPRequest req = new TICKMAPRequest(type,message);
            private Configuration configuration = new Configuration();
            private String keystorePassword;
            private ResultSet rs;
            private PublicKey clientPK;
            private PublicKey serverPK;
            private PrivateKey serverPrK;
            private SecretKey authenticationK;
            private SecretKey cipherK;
            private int idVol=0;
            private int idPassager=0;
            private int prixTotal= 0;
            @Override
            public void run() 
            {
                getKeys();
                boolean disconnected = false;
                while(!disconnected)
                {
                    switch(req.getType())
                    {
                        case REQUEST_LOGIN : 
                            treatLogin();
                            break;
                        case REQUEST_LOGOUT : 
                            treatLogout();
                            disconnected = true;
                            break;
                        case REQUEST_HANDSHAKE : 
                            treatHandshake();
                            break;
                        case REQUEST_BOOKFLY : 
                            treatBookFly();
                            break;
                        case REQUEST_CONFIRMFLY :
                            treatConfirmFly();
                            break;
                        case REQUEST_CONFIRMPAY :
                            treatConfirmPay();
                        break;
                    }
                    try 
                    {
                        if(rep!= null)
                            oos.writeObject(rep);
                        rep= null;
                        req = (TICKMAPRequest) ois.readObject();
                    } 
                    catch (ClassNotFoundException ex) 
                    {
                        Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                        disconnected = true;
                    } 
                    catch (IOException ex) 
                    {
                        System.out.println("Client socket closed");
                        disconnected = true;
                    }
                }
                
            }

            private void treatLogin() 
            {
                LoginMessage m = (LoginMessage)req.message;
                if(state == "AUTHENTICATED" || state =="HANDSHAKED")
                {
                    rep = new TICKMAPResponse(TICKMAPResponse.FAILED,new SimpleMessage("Déjà authentifié"));
                    return;
                }
                try 
                {
                    rs = bd.executeQuery("select password from agents where login ='"+m.getLogin()+"'");
                    if(rs.next())
                    {
                        String pass = rs.getString("password");
                        System.out.println("l = "+m.getLogin()+" r = "+m.getRandom()+" time = "+m.getTime()+"");
                        byte[] msgD = buildDigest(m.getTime(),m.getRandom(),pass,m.getLogin());
                        
                        if(MessageDigest.isEqual(msgD, m.getDigest()))
                        {
                            cs.trace("server#"+m.getLogin()+" login ok");
                            state = "AUTHENTICATED";
                            rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS,new SimpleMessage("Connexion réussie avec succès"));
                        }
                        else
                        {
                            cs.trace("server#"+m.getLogin()+" login not ok -> Digests ne correspondent pas");
                            rep = new TICKMAPResponse(TICKMAPResponse.FAILED,new SimpleMessage("Mot de passe incorrecte"));
                        }
                    }
                    else
                    {
                        cs.trace("server#"+m.getLogin()+" login not ok -> login innéxistant");
                        rep = new TICKMAPResponse(TICKMAPResponse.FAILED,new SimpleMessage("Le login n'existe pas"));
                    }
                    rs.close();
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            private byte[] buildDigest(long time,double r,String password,String login)
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
                } 
                catch (IOException | NoSuchAlgorithmException | NoSuchProviderException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return msgD;
            }
            
            private void treatLogout()
            {
                if(state == "NON_AUTHENTICATED") 
                {
                    rep = new TICKMAPResponse(TICKMAPResponse.FAILED,new SimpleMessage("Vous n'êtes pas authentifié"));
                    cs.trace("server#Utilisateur non authentifié !");
                    return;
                }
                state = "NON_AUTHENTICATED";
                rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS, new SimpleMessage("Déconnexion réussie !"));
                cs.trace("server#client deconnecté");
            }

            private void treatHandshake() 
            {
                cs.trace("server#Handshake");
                if(generateSecretKeys())
                {
                    try
                    {
                        cs.trace("server#Clés secrètes générées");
                        byte[] cipherKBytes = cipherK.getEncoded();
                        
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                        cipher.init(Cipher.ENCRYPT_MODE, clientPK);
                        
                        byte[] authKeyEncrypted = cipher.doFinal(authenticationK.getEncoded());
                        byte[] cipherKeyEncrypted = cipher.doFinal(cipherK.getEncoded());
                        state = "HANDSHAKED";
                        rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS, new HandshakeMessage(authKeyEncrypted,cipherKeyEncrypted));
                        oos.writeObject(rep);
                        getFlies();
                        return;
                    } 
                    catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException ex)
                    {
                        Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                rep = new TICKMAPResponse(TICKMAPResponse.FAILED, new SimpleMessage("Erreur lors de la génération de clés secrètes !"));
                try 
                {
                    oos.writeObject(rep);
                } catch (IOException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            public void getFlies()
            {
                try 
                {
                    cs.trace("server#Récupération des vols des 7 prochains jours !");
                    rs = bd.executeQuery("SELECT * FROM vols WHERE DATE(HeureDepart) < CURDATE() + INTERVAL 7 DAY AND Date(HeureDepart) >= CURDATE() AND PlaceRestante >0");
                    Vector<Fly> flies = new Vector<Fly>();
                    while(rs.next())
                    {
                        flies.add(new Fly(rs.getInt("idVol"),rs.getString("destination"),rs.getInt("PrixPlace"),rs.getTimestamp("HeureDepart"),rs.getString("depart"),rs.getInt("PlaceRestante")));
                        System.out.println(flies.lastElement());
                    }
                    rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS,new FlyMessage(flies));
                    rs.close();
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            private void getKeys() 
            {
                try 
                {
                    cs.trace("server#Récupération des clés dans le keystore !");
                    keystorePassword = configuration.getPropertie("KEYSTORE_PASS");
                    KeyStore ks = KeyStore.getInstance("jks");
                    ks.load(new FileInputStream("../Resources/ServeurCrypto/InpresAirportServeur.keystore"),keystorePassword.toCharArray());
                    serverPrK = (PrivateKey) ks.getKey("serveur", keystorePassword.toCharArray());
                    X509Certificate cert = (X509Certificate) (ks.getCertificate("clientcertificat"));
                    clientPK = cert.getPublicKey();
                }
                catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                    cs.trace("server#Erreur lors de la génération des clés secrètes !");
                }
            }

            private boolean generateSecretKeys() 
            {
                
                try 
                {
                    KeyGenerator keyGen = KeyGenerator.getInstance("Rijndael", "BC");
                    keyGen.init(new SecureRandom());
                    cipherK = keyGen.generateKey();
                    authenticationK = keyGen.generateKey();
                    System.out.println("Server auth : "+Base64.getEncoder().encodeToString(authenticationK.getEncoded()));
                    System.out.println("Server cipher : "+Base64.getEncoder().encodeToString(cipherK.getEncoded()));
                    return true;
                } 
                catch (NoSuchAlgorithmException | NoSuchProviderException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                cs.trace("server#Erreur lors de la génération des clés secrètes !");
                return false;
            }

            private void treatBookFly() 
            {
                cs.trace("server#Réservation d'un vol !");
                BookMessage message = (BookMessage) req.getMessage();
                int numeroBillet = 0;
                Cipher cipher;
                try 
                {
                    cipher = Cipher.getInstance("Rijndael/ECB/PKCS5Padding","BC");
                    cipher.init(Cipher.DECRYPT_MODE,cipherK);
                    
                    byte[] idVolBytes = cipher.doFinal(message.getIdVol());
                    byte[] vVoyageurBytes = cipher.doFinal(message.getvVoyageur());
                    
                    ByteArrayInputStream  bais = new ByteArrayInputStream(vVoyageurBytes);
                    ObjectInputStream i = new ObjectInputStream(bais);
                    
                    Vector<Voyageur> vVoyageur = (Vector<Voyageur>)i.readObject();
                    
                    bais = new ByteArrayInputStream(idVolBytes);
                    DataInputStream dis = new DataInputStream(bais);
                    idVol = dis.readInt();
                    
                    Voyageur v = vVoyageur.elementAt(0);//voyageur de référence
                    rs = bd.executeQuery("SELECT * from passager where numeroID="+v.getNumeroID());
                    if(rs.next())
                    {
                        idPassager = rs.getInt("idPassager");
                    }
                    else
                    {
                        bd.insertQuery("INSERT INTO PASSAGER (Nom,Prenom,NumeroID) values ('"+v.getNom()+"','"+v.getPrenom()+"','"+v.getNumeroID()+"')");
                        rs = bd.executeQuery("SELECT * from passager where numeroID="+v.getNumeroID());
                        if(rs.next())
                            idPassager = rs.getInt("idPassager");
                    }
                    synchronized(bd)
                    {
                        bd.insertQuery("INSERT INTO RESERVATION (`idPassager`, `Place`, `idVol`) VALUES('"+idPassager+"',"+vVoyageur.size()+","+idVol+")");
                        bd.insertQuery("UPDATE vols set PlaceRestante = PlaceRestante - "+vVoyageur.size()+" where idVol = "+idVol+"");
                        bd.insertQuery("INSERT into billets (idPassager, NombrePassager, idVol) values ("+idPassager+","+vVoyageur.size()+","+idVol+")");
                    }
                    rs = bd.executeQuery("SELECT * FROM BILLETS where idvol ="+idVol+" AND idPassager="+idPassager);
                    if(rs.next())
                    {
                        numeroBillet = rs.getInt("NumeroBillet");
                    }
                    rs = bd.executeQuery("SELECT * FROM vols where idVol = "+idVol);
                    if(rs.next())
                    {
                        prixTotal = vVoyageur.size() * rs.getInt("PrixPlace");
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeInt(numeroBillet);
                    dos.writeInt(prixTotal);
                    cipher.init(Cipher.ENCRYPT_MODE,cipherK);
                    rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS,new BookFlyResponseMessage(cipher.doFinal(baos.toByteArray())));
                    rs.close();
                } 
                catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException | ClassNotFoundException | SQLException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void treatConfirmFly() 
            {
                ConfirmFlyMessage message = (ConfirmFlyMessage)req.getMessage();
                String m = message.getMessage();
                byte[] hmacRecu = message.getHmac();
                
                try 
                {
                    Mac hLocal = Mac.getInstance("HMAC-MD5","BC");
                    hLocal.init(authenticationK);
                    hLocal.update(m.getBytes());
                    byte[] hbLocal = hLocal.doFinal();
                    if(MessageDigest.isEqual(hmacRecu, hbLocal))
                    {
                        cs.trace("server#HMAC vérifié");
                        rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS);
                    }
                    else
                    {
                        cs.trace("server#HMAC incorrecte");
                        rep = new TICKMAPResponse(TICKMAPResponse.FAILED);
                    }
                }
                catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException ex)
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void treatConfirmPay() 
            {
                ConfirmPayMessage message = (ConfirmPayMessage) req.getMessage();
                byte[] encryptedCarte = message.getCarte();
                byte[] hmacRecu = message.getHmac();
                
                try 
                {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                    cipher.init(Cipher.DECRYPT_MODE, serverPrK);
                    
                    String carte = new String(cipher.doFinal(encryptedCarte));
                    System.out.println("Carte sur serveur = "+carte);
                    
                    Mac hLocal = Mac.getInstance("HMAC-MD5","BC");
                    hLocal.init(authenticationK);
                    hLocal.update(carte.getBytes());
                    byte[] hbLocal = hLocal.doFinal();
                    if(MessageDigest.isEqual(hmacRecu, hbLocal))
                    {
                        cs.trace("server#HMAC vérifié");
                        if(message.getType()==message.SUCCESS)
                        {
                            bd.insertQuery("DELETE FROM RESERVATION WHERE idPassager = "+idPassager+" AND idVol = "+idVol);
                            bd.insertQuery("INSERT INTO facture (login,total,carte) VALUES('"+idPassager+"',"+prixTotal+","+carte+")");
                            rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS);
                        }
                        else
                        {
                            cancelBook();
                            bd.insertQuery("INSERT INTO facture (login,total,carte) VALUES('"+idPassager+"',"+prixTotal+",'"+carte+" payement refusé !')");
                        }
                    }
                    else
                    {
                        cs.trace("server#HMAC incorrecte");
                        rep = new TICKMAPResponse(TICKMAPResponse.FAILED);
                        cancelBook();
                    }
                }
                catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | SQLException ex)
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void cancelBook() 
            {
                try 
                {
                    rs = bd.executeQuery("SELECT * FROM reservation where idPassager = "+idPassager+" AND idVol = "+idVol);
                    while(rs.next())
                    {
                        bd.insertQuery("UPDATE vols set PlaceRestante = PlaceRestante + "+rs.getInt("Place")+" where idVol = "+rs.getInt("idVol"));
                    }
                    bd.insertQuery("DELETE FROM RESERVATION WHERE idPassager = "+idPassager+" AND idVol = "+idVol);
                    bd.insertQuery("DELETE FROM BILLETS WHERE idPassager = "+idPassager+" AND idVol = "+idVol);
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        };
    }
    public int getType()
    {
        return type;
    }
}
