/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadik
 */
class CheckBaggages extends Thread
{
     private int port;
    private BeanBDAccess bd;
    private ServerSocket SSocket;
    private Socket CSocket;
    private int nbrClient;
    private Configuration configuration = new Configuration();
    private String sep;
    private String end;
    private static final int CHECK_BAGGAGE = 3;
    private static final int CHECK_SUCCESS = 100;
    private static final int CHECK_FAILED = 101;
    DataInputStream dis=null;
    DataOutputStream dos = null;
    CheckBaggages(int p, BeanBDAccess bd) {
        port = p;
        this.bd = bd;
        nbrClient = Integer.parseInt(configuration.getPropertie("POOL_NUMBER"));
        sep = configuration.getPropertie("TRAME_SEPARATOR");
        end = configuration.getPropertie("END_TRAME");
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
        while(!isInterrupted())
        {
            try 
            {
                CSocket = SSocket.accept();
                System.out.println("Connexion accepté");
                dis = new DataInputStream(CSocket.getInputStream());
                dos = new DataOutputStream(CSocket.getOutputStream());
            }
            catch (IOException ex)
            {
                Logger.getLogger(CheckTicketThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            while(CSocket.isConnected())
            {
                String message = readMessage();
                if(message==null)
                    break;
                String[] messageSplit = message.split("\\"+sep);
                System.out.println("message = "+message+"  0 = "+messageSplit[0]+" sep = "+sep);
                if(Integer.parseInt(messageSplit[0]) == CHECK_BAGGAGE)
                {
                    int idVol = Integer.parseInt(messageSplit[1]);

                    ResultSet rs = bd.executeQuery("select * from bagages where idBillets in (select numeroBillet from billets where idvol = "+idVol+") and charge !='O'");
                    try 
                    {
                        if(rs.next())
                        {
                            sendMessage(CHECK_FAILED+end);
                        }
                        else
                        {
                            sendMessage(CHECK_SUCCESS+end);
                        }
                        rs.close();
                    } 
                    catch (SQLException ex) 
                    {
                        Logger.getLogger(CheckTicketThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else
                {
                    try {
                        CSocket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(CheckTicketThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("message recu : "+message);
            }
        }
        try 
        {
                if(CSocket!=null)
                    CSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(CheckTicketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String readMessage()
    {
        StringBuffer message=new StringBuffer();
        byte b;
        try {
            
            while ((b=dis.readByte())!= (byte)end.charAt(0) )
            {
                if (b!=end.charAt(0))
                    message.append((char) b);
            }
            
        } 
        catch (IOException ex) 
        {
            return null;
        }
        return message.toString().trim();
    }
    public void sendMessage(String s)
    {
        try 
        {
            dos.write(s.trim().getBytes());
            dos.flush();
        }
        catch (IOException ex) {
            Logger.getLogger(CheckTicketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
