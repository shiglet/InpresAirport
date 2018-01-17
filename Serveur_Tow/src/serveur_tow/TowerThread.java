/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur_tow;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadik
 */
public class TowerThread extends Thread 
{
    private static final int GET_FLY = 1;
    private Configuration config = new Configuration();
    private Socket CSocket;
    private ServerSocket SSocket;
    private int port;
    private String addresse;
    private volatile int nbrThread = 0;
    private String sep;
    private String end;
    BeanBDAccess bd;
    public TowerThread(BeanBDAccess bd)
    {
        port = Integer.parseInt(config.getPropertie("PORT_TOWER"));
        sep = config.getPropertie("TRAME_SEPARATOR");
        end = config.getPropertie("END_TRAME");
        this.bd = bd;
    }
    public void run()
    {
        try
        {
            SSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(!isInterrupted())
        {
            try
            {
                CSocket = SSocket.accept();
                System.out.println("Client connecté");
            } 
            catch (IOException ex) 
            {
                System.err.println("Erreur lors du accept" + ex.getMessage());
                System.exit(1);
            }
            nbrThread++;
            if(nbrThread<5)
            {
                Thread th = new Thread(new Runnable()
                {
                    private DataOutputStream dos;
                    private DataInputStream dis;
                    @Override
                    public void run()
                    {
                        try 
                        {
                            dos = new DataOutputStream(CSocket.getOutputStream());
                            dis = new DataInputStream(CSocket.getInputStream());
                            boolean finished = false;
                            while(!finished)
                            {
                                String message = readMessage();
                                String[] messageSplit = message.split("\\"+sep);
                                int type = Integer.parseInt(messageSplit[0]);
                                System.out.println("message reçu : "+message);
                                switch(type)
                                {
                                    case GET_FLY : 
                                        message = GET_FLY+"";
                                        ResultSet rs = bd.executeQuery("SELECT * FROM vols WHERE HeureDepart < NOW() + INTERVAL 3 HOUR AND HeureDepart >= NOW()");
                                        while(rs.next())
                                        {
                                            int idVol = rs.getInt("idVol");
                                            String destination = rs.getString("destination");
                                            String depart = rs.getString("depart");
                                            Timestamp date = rs.getTimestamp("HeureDepart");
                                            message = message+sep+idVol+sep+destination+sep+depart+sep+date;
                                        }
                                    break;
                                }
                                System.out.println("Envoie de "+message);
                                sendMessage(message+end);
                            }
                            nbrThread--;
                        } 
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    public String readMessage()
                    {
                        StringBuffer message=new StringBuffer();
                        try 
                        {
                            byte b;
                            while ((b=dis.readByte())!= (byte)end.charAt(0) )
                            {
                                if (b!=end.charAt(0))
                                    message.append((char) b);
                            }
                            
                        } 
                        catch (IOException ex) {
                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                        return message.toString().trim();
                    }
                    public void sendMessage(String s)
                    {
                        try {
                            dos.write(s.trim().getBytes());
                            dos.flush();
                        } catch (IOException ex) {
                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                th.run();
            }
        }
    }
}
