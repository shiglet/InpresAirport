/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import java.net.Socket;
import interfaces.Request;
import interfaces.ServerConsole;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import response.LUGAPResponse;

/**
 *
 * @author Sadik
 */
public class LUGAPRequest implements Request, Serializable{
    public static final short REQUEST_LOGIN = 1;
    public static final short REQUEST_LOGOUT = 2;
    public static final short REQUEST_FLYLIST = 3;
    private byte[] bytes;
    private short type;
    private String data;
    public LUGAPRequest(short t,String d)
    {
        type = t;
        data = d;
    }
    public LUGAPRequest(short t,String d,byte[] b)
    {
        type = t;
        data = d;
        bytes = b;
    }
    public LUGAPRequest(short t)
    {
        type = t;
    }

    private LUGAPRequest() {
        }
    
    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs,ObjectOutputStream oos, ObjectInputStream ois,BeanBDAccess beanBD) {
        return new Runnable() {
            private BeanBDAccess bd;
            private LUGAPResponse rep;
            private LUGAPRequest req = new LUGAPRequest();
            private Configuration configuration = new Configuration();
            private String sep;
            @Override
            public void run()
            {
                bd = beanBD;
                req.type = type;
                req.bytes = bytes;
                sep = "\\"+configuration.getPropertie("TRAME_SEPARATOR");
                boolean disconnected = false;
                while(!disconnected)
                {
                    switch(req.type)
                    {
                        case REQUEST_LOGIN :
                            treatLogin();
                            break;
                        case REQUEST_FLYLIST : 
                            getFly();
                            break;
                        case REQUEST_LOGOUT : 
                            disconnected = true;
                            treatLogout();
                            break;
                    }
                    
                    try 
                    {
                        if(rep!= null)
                            oos.writeObject(rep);
                        rep= null;
                        req = (LUGAPRequest) ois.readObject();
                    } 
                    catch (IOException | ClassNotFoundException ex) 
                    {
                        Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                        disconnected = true;
                    }
                }
            }

            private void treatLogin() 
            {
                String[] tokens = data.split(sep);
                if(tokens.length < 3)
                {
                    rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Erreur argument");
                    cs.trace("server#Erreur argument");
                    return;
                }
                String login = tokens[0];
                long time = Long.parseLong(tokens[1]);
                double r = Double.parseDouble(tokens[2]);
                
                ResultSet rs = bd.executeQuery("select password from agents where login ='"+tokens[0]+"'");
                try 
                {
                    if(rs.next())
                    {
                        String pass = rs.getString("password");
                        byte[] msgD = buildDigest(time,r,pass,login);
                        
                        if(MessageDigest.isEqual(msgD, req.bytes))
                        {
                            cs.trace("server#"+tokens[0]+" login ok");
                            rep = new LUGAPResponse(LUGAPResponse.LOGIN_SUCCESS,"Connexion réussie avec succès");
                        }
                        else
                        {
                            cs.trace("server#"+tokens[0]+" login not ok");
                            rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Mot de passe incorrecte");
                        }
                    }
                    else
                    {
                        cs.trace("server#"+tokens[0]+" login not ok");
                        rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Le login n'existe pas");
                    }
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
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
                } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException ex) {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return msgD;
            }

            private void treatLogout() 
            {
                
            }

            private Vector<String> getFly() 
            {
                try 
                {
                    ResultSet rs = bd.executeQuery("SELECT * FROM vols WHERE DATE(HeureDepart) = CURDATE()");
                    Vector<String> flies = new Vector<String>();
                    while(rs.next())
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
                        flies.add("VOL "+rs.getString("idVol") + " - "+ rs.getString("Destination") + " " +dateFormat.format(rs.getTime("HeureDepart")));
                        cs.trace("server#"+flies.lastElement());
                    }
                    rep = new LUGAPResponse(LUGAPResponse.FLYLIST_SUCCESS);
                    rep.vData = flies;
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            
        };
    }
    
}
