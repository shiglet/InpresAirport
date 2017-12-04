/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import request.LUGAPRequest;

/**
 *
 * @author Sadik
 */
public class ChatThread extends Thread {
    private  int PORT_FOR_US_ONLY ;
    private static int PORT_CHAT = 20027;
    private  String ADRESSE ;
    private static int LOGIN_GROUP = 1;
    private static int LOGIN_OK = 2;
    private static int LOGIN_NOK = 3;
    private String nom;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket CSocket;
    private ServerSocket SSocket;
    private String sep,end;
    private BeanBDAccess bd;
    private MulticastSocket socketGroupe;
    private InetAddress adresseGroupe;
    public ChatThread(BeanBDAccess bd)
    {
        Configuration config = new Configuration();
        PORT_FOR_US_ONLY = Integer.parseInt(config.getPropertie("PORT_CHAT"));
        PORT_CHAT = Integer.parseInt(config.getPropertie("PORT_MULTICAST"));
        ADRESSE = config.getPropertie("ADRESSE_MULTICAST");
        sep = config.getPropertie("TRAME_SEPARATOR");
        end = config.getPropertie("END_TRAME");
        try
        {
            adresseGroupe = InetAddress.getByName(ADRESSE);
            socketGroupe = new MulticastSocket(PORT_CHAT);
            socketGroupe.joinGroup(adresseGroupe);
            System.out.println("MULTICAST REJOINT");
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.bd = bd;
    }
    public void run()
    {
        try 
        {
            SSocket = new ServerSocket(PORT_FOR_US_ONLY);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (!isInterrupted())
        {
            try
            {
                System.out.println("***SERVEUR CHAT EN ATTENTE SUR LE PORT "+PORT_FOR_US_ONLY+"***");
                CSocket = SSocket.accept();
            } 
            catch (IOException ex) 
            {
                System.err.println("Erreur CSocket Accepte " + ex.getMessage());
                System.exit(1);
            }
            ObjectInputStream ois=null;
            ObjectOutputStream oos=null;
            try
            {
                dos = new DataOutputStream(CSocket.getOutputStream());
                dis = new DataInputStream(CSocket.getInputStream());
            }
            catch (IOException e)
            {
                System.err.println("Erreur ? [" + e.getMessage() + "]");
            }
            String message = readMessage();
            String[] messageSplit = message.split("\\"+sep);
            System.out.println(messageSplit[0]);
            int type = Integer.parseInt(messageSplit[0]);
            
            if(type==LOGIN_GROUP)
            {
                String login = messageSplit[1];
                /*
                //Bouncy Castle
                int l = Integer.parseInt(messageSplit[2]);
                byte[] pwdHRecu = readDigest(l);
                System.out.println((Arrays.toString(pwdHRecu)));
                long time = Long.parseLong(messageSplit[3]);
                double r = Double.parseDouble(messageSplit[4]);
                System.out.println(pwdHRecu+" t = "+time+" random = "+r);
                System.out.println("Login = "+login);
                ResultSet rs;
                rs = bd.executeQuery("SELECT * from clients where login = '"+login+"'");
                try 
                {
                    if (!rs.next() )
                    {
                        sendMessage(LOGIN_NOK+sep+"Le login n'existe pas"+end);
                    } 
                    else
                    {
                        String pwd = rs.getString("password");
                        byte[] pwdHCalcule = buildDigest(time,r,pwd,login);
                        if(MessageDigest.isEqual(pwdHRecu,pwdHCalcule))
                            sendMessage(LOGIN_OK+sep+ADRESSE+sep+PORT_CHAT+end);
                        else sendMessage(LOGIN_NOK+sep+"Mot de passe incorrecte!"+end);
                    }
                } 
                catch (SQLException ex) {
                    Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
                }*/
                //Hashage simple
                int hashRecu = Integer.parseInt(messageSplit[2]);
                int r = Integer.parseInt(messageSplit[3]);
                ResultSet rs;
                rs = bd.executeQuery("SELECT * from clients where login = '"+login+"'");
                try 
                {
                    if (!rs.next() )
                    {
                        sendMessage(LOGIN_NOK+sep+"Le login n'existe pas"+end);
                    } 
                    else
                    {
                        String pwd = rs.getString("password");
                        if(hashage(pwd,r) == hashRecu)
                            sendMessage(LOGIN_OK+sep+ADRESSE+sep+PORT_CHAT+end);
                        else sendMessage(LOGIN_NOK+sep+"Mot de passe incorrecte!"+end);
                    }
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(ChatThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
            Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msgD;
    }
    public byte[] readDigest(int l)
    {
        byte[] b = new byte[l];
        try 
        {
            dis.readFully(b);
        } 
        catch (IOException ex) {
            System.err.println("Erreur du readMessage dans ThreadClientMV :  "+ex);
        }
        return b;
    }
    public String readMessage()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte b;
        try {
            
            while ((b=dis.readByte())!= (byte)end.charAt(0))
            {
                if (b!=(byte)end.charAt(0))
                    out.write(b);
            }
            
        } 
        catch (IOException ex) {
            System.err.println("Erreur du readMessage dans ThreadClientMV :  "+ex);
        }
        return new String(out.toByteArray()).trim();
    }
    public void sendMessage(String s)
    {
        try 
        {
            System.out.println(nom+">>Envoie de : "+s);
            dos.write(s.trim().getBytes());
            dos.flush();
        }
        catch (IOException ex) {
            System.err.println("Erreur du sendMessage dans ThreadClientMV :  "+ex);
        }
    }
    public int hashage(String s, int r)
    {
        int sum=0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            sum = sum + (int) c;
        }
        sum = (sum + r % 67)*r;
        return sum;
    }
}
