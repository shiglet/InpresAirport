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
    private static final int WARN_CHECKIN = 2;
    public static final int CHECK_BAGGAGE = 3;
    public static final int CHOOSE_FLY = 4;
    public static final int GET_PISTE = 5;
    public static final int CHOOSE_PISTE = 6;
    private static final int SUCCESS = 100;
    private static final int FAILED = 101;
    private static final int TAKING_OFF = 7;
    private static final int FLYING = 8;
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
                System.out.println("Attente d'un client");
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
                    private int currentFly=0;
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
                                if(message==null)
                                {
                                    System.out.println("finii !");
                                    finished = true;
                                    continue;
                                }
                                String[] messageSplit = message.split("\\"+sep);
                                int type = Integer.parseInt(messageSplit[0]);
                                System.out.println("message reçu : "+message);
                                switch(type)
                                {
                                    case GET_FLY : 
                                        message = GET_FLY+"";
                                        ResultSet rs = bd.executeQuery("SELECT * FROM vols WHERE HeureDepart < NOW() + INTERVAL 3 HOUR AND HeureDepart >= NOW() AND idavion in (SELECT idAvion from avion where etat = 'libre')");
                                        while(rs.next())
                                        {
                                            int idVol = rs.getInt("idVol");
                                            String destination = rs.getString("destination");
                                            String depart = rs.getString("depart");
                                            Timestamp date = rs.getTimestamp("HeureDepart");
                                            message = message+sep+idVol+sep+destination+sep+depart+sep+date;
                                        }
                                    break;
                                    case CHOOSE_FLY : 
                                        currentFly = Integer.parseInt(messageSplit[1]);
                                        rs = bd.executeQuery("SELECT * FROM vols WHERE HeureDepart < NOW() + INTERVAL 3 HOUR AND HeureDepart >= NOW() AND idavion in (SELECT idAvion from avion where etat = 'libre') and idVol = "+currentFly);
                                        if(rs.next())
                                        {
                                            bd.insertQuery("UPDATE avion set etat ='BUSY' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                            message = ""+SUCCESS;
                                        }
                                        else
                                            message = ""+FAILED;
                                        break;
                                    case WARN_CHECKIN : 
                                        try
                                        {
                                            Socket s = new Socket(config.getPropertie("CHECKINIPURGENCE"),Integer.parseInt(config.getPropertie("CHECKIPORTURGENCE")));
                                            DataOutputStream d= new DataOutputStream(s.getOutputStream());
                                            String m = 1000+end;
                                            d.write(m.trim().getBytes());
                                            d.flush();
                                            s.close();
                                        }
                                        catch (IOException ex)
                                        {
                                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        bd.insertQuery("UPDATE avion set etat ='CHECKIN_OFF' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                        message = SUCCESS+"";
                                        break;
                                    case CHECK_BAGGAGE : 
                                        try 
                                        {
                                            Socket s = new Socket(config.getPropertie("SERVER_IP"),Integer.parseInt(config.getPropertie("URGENCEBAGGAGE_PORT")));
                                            DataOutputStream d= new DataOutputStream(s.getOutputStream());
                                            DataInputStream i = new DataInputStream(s.getInputStream());
                                            message = message + end;
                                            d.write(message.trim().getBytes());
                                            d.flush();
                                            message = readMessage(i);
                                            System.out.println("Check baggage de "+message);
                                            if(Integer.parseInt(message) == SUCCESS)
                                            {
                                                
                                                bd.insertQuery("UPDATE avion set etat ='READY' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                                message =SUCCESS+"";
                                            }
                                            else
                                            {
                                                message =FAILED+"";
                                            }
                                            s.close();
                                        } 
                                        catch (IOException ex) 
                                        {
                                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        break;
                                    case GET_PISTE :
                                        rs = bd.executeQuery("SELECT * FROM piste where etat ='libre'");
                                        boolean vide = true;
                                        message = SUCCESS+"";
                                        while(rs.next())
                                        {
                                            vide = false;
                                            message = message+sep+rs.getString("nom");
                                        }
                                        if(vide)
                                        {
                                            message = FAILED+"";
                                        }
                                        break;
                                    case CHOOSE_PISTE :
                                        rs = bd.executeQuery("SELECT * FROM piste where etat ='libre' and nom ='"+messageSplit[1]+"'");
                                        if(rs.next())
                                        {
                                            bd.insertQuery("UPDATE avion set etat ='READY_TO_FLY' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                            bd.insertQuery("UPDATE piste set etat ='occupé' where nom = '"+messageSplit[1]+"'");
                                            message = SUCCESS+"";
                                        }
                                        else
                                            message = FAILED+"";
                                        break;
                                    case TAKING_OFF : 
                                        bd.insertQuery("UPDATE avion set etat ='TAKING_OFF' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                        message = SUCCESS+"";
                                        break;
                                    case FLYING :
                                        bd.insertQuery("UPDATE avion set etat ='FLYING' where idAvion in (SELECT idAvion from vols where idvol = "+currentFly+")");
                                        message = SUCCESS+"";
                                        
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
                            return null;
                        }
                        return message.toString().trim();
                    }
                    public String readMessage(DataInputStream i)
                    {
                        StringBuffer message=new StringBuffer();
                        try 
                        {
                            byte b;
                            while ((b=i.readByte())!= (byte)end.charAt(0) )
                            {
                                if (b!=end.charAt(0))
                                    message.append((char) b);
                            }
                            
                        } 
                        catch (IOException ex) {
                            return null;
                        }
                        return message.toString().trim();
                    }
                    public void sendMessage(String s)
                    {
                        try {
                            dos.write(s.trim().getBytes());
                            dos.flush();
                        } catch (IOException ex) 
                        {
                            Logger.getLogger(TowerThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                th.start();
            }
        }
    }
}
