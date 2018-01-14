/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import interfaces.Request;
import interfaces.ServerConsole;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.LoginMessage;
import message.Message;
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
            private ResultSet rs;
            
            @Override
            public void run() 
            {
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
                if(state == "AUTHENTICATED")
                {
                    rep = new TICKMAPResponse(TICKMAPResponse.FAILED,"Déjà authentifié");
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
                            rep = new TICKMAPResponse(TICKMAPResponse.SUCCESS,"Connexion réussie avec succès");
                        }
                        else
                        {
                            cs.trace("server#"+m.getLogin()+" login not ok");
                            rep = new TICKMAPResponse(TICKMAPResponse.FAILED,"Mot de passe incorrecte");
                        }
                    }
                    else
                    {
                        cs.trace("server#"+m.getLogin()+" login not ok");
                        rep = new TICKMAPResponse(TICKMAPResponse.FAILED,"Le login n'existe pas");
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
                try 
                {
                    if(state == "NON_AUTHENTICATED")
                    {
                        rep = new TICKMAPResponse(TICKMAPResponse.FAILED,"Vous n'êtes pas authentifié");
                        oos.writeObject(new TICKMAPResponse(TICKMAPResponse.SUCCESS));
                        return;
                    }
                    oos.writeObject(new TICKMAPResponse(TICKMAPResponse.SUCCESS));
                    cs.trace("server#client deconnecté");
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(TICKMAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void treatHandshake() 
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
    }
    public int getType()
    {
        return type;
    }
}
