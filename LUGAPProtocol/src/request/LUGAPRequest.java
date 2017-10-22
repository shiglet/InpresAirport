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
import models.LuggageModel;
import response.LUGAPResponse;

/**
 *
 * @author Sadik
 */
public class LUGAPRequest implements Request, Serializable{
    private static final long serialVersionUID = -5759325886598747695L;
    public static final short REQUEST_LOGIN = 1;
    public static final short REQUEST_LOGOUT = 2;
    public static final short REQUEST_FLYLIST = 3;
    public static final short REQUEST_LUGGAGES = 4;
    public static final short UPDATE_LUGGAGE = 5;
    public LuggageModel luggage;
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
    public LUGAPRequest(short t,LuggageModel l)
    {
        type = t;
        luggage = l;
    }

    private LUGAPRequest() {
        }
    
    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs,ObjectOutputStream oos, ObjectInputStream ois,BeanBDAccess beanBD) {
        return new Runnable() {
            private String state = "NON_AUTHENTICATED";
            private BeanBDAccess bd;
            private LUGAPResponse rep;
            private LUGAPRequest req = new LUGAPRequest();
            private Configuration configuration = new Configuration();
            private ResultSet rs;
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
                        case REQUEST_LUGGAGES :
                            getLuggages(req.data);
                            break;
                        case UPDATE_LUGGAGE:
                            updateLuggage();
                            break;
                        case REQUEST_LOGOUT : 
                            disconnected = true;
                            treatLogout();
                            continue;
                    }
                    
                    try 
                    {
                        if(rep!= null)
                            oos.writeObject(rep);
                        rep= null;
                        req = (LUGAPRequest) ois.readObject();
                    } 
                    catch (ClassNotFoundException ex) 
                    {
                        Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                        disconnected = true;
                    } 
                    catch (IOException ex) 
                    {
                        System.out.println("Client socket closed");
                        disconnected = true;
                    }
                }
                try 
                {
                    oos.close();
                    ois.close();
                    s.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                cs.trace("server#fin runnable");
            }

            private void treatLogin() 
            {
                if(state == "AUTHENTICATED")
                {
                    rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Déjà authentifié");
                    return;
                }
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
                
                try 
                {
                    bd.insertQuery("LOCK TABLE agents READ");
                    rs = bd.executeQuery("select password from agents where login ='"+tokens[0]+"'");
                    bd.insertQuery("UNLOCK TABLES");
                    if(rs.next())
                    {
                        String pass = rs.getString("password");
                        byte[] msgD = buildDigest(time,r,pass,login);
                        
                        if(MessageDigest.isEqual(msgD, req.bytes))
                        {
                            cs.trace("server#"+tokens[0]+" login ok");
                            state = "AUTHENTICATED";
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
                    rs.close();
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
                } 
                catch (IOException | NoSuchAlgorithmException | NoSuchProviderException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return msgD;
            }

            private void treatLogout() 
            {
                try 
                {
                    if(state == "NON_AUTHENTICATED")
                    {
                        rep = new LUGAPResponse(LUGAPResponse.LOGOUT_FAILED,"Vous n'êtes pas authentifié");
                        oos.writeObject(new LUGAPResponse(LUGAPResponse.LOGOUT_SUCCESS));
                        return;
                    }
                    oos.writeObject(new LUGAPResponse(LUGAPResponse.LOGOUT_SUCCESS));
                    cs.trace("server#client deconnecté");
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void getFly() 
            {
                if(state != "AUTHENTICATED")
                {
                    rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Veuillez vous authentifier d'abord");
                    return;
                }
                try 
                {
                    bd.insertQuery("LOCK TABLE vols READ");
                    rs = bd.executeQuery("SELECT * FROM vols WHERE DATE(HeureDepart) = CURDATE()");
                    bd.insertQuery("UNLOCK TABLE");
                    Vector<String> flies = new Vector<String>();
                    while(rs.next())
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
                        flies.add("VOL "+rs.getString("idVol") + " - "+ rs.getString("Destination") + " " +dateFormat.format(rs.getTime("HeureDepart")));
                        cs.trace("server#"+flies.lastElement());
                    }
                    rep = new LUGAPResponse(LUGAPResponse.FLYLIST_SUCCESS);
                    rep.vData = flies;
                    rs.close();
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void getLuggages(String idVol) 
            {
                if(state != "AUTHENTICATED")
                {
                    rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Veuillez vous authentifier d'abord");
                    return;
                }
                try 
                {
                    bd.insertQuery("LOCK TABLES bagages READ,billets READ");
                    rs = bd.executeQuery("select * from bagages where numerobillet in (select numerobillet from billets where idvol = '"+idVol+"')");
                    System.out.println(idVol);
                    bd.insertQuery("UNLOCK TABLES");
                    Vector<LuggageModel> vData = new Vector<LuggageModel>();
                    while(rs.next())
                    {
                        System.out.println("On est dedans");
                        double p;
                        String id,r,recep,d,c,b,v;
                        id = rs.getString("idBagages");
                        r = rs.getString("remarques");
                        recep = rs.getString("receptionne");
                        d = rs.getString("douane");
                        c = rs.getString("charge");
                        b = rs.getString("NumeroBillet");
                        v = rs.getString("valise");
                        p = rs.getDouble("poids");
                        LuggageModel luggage = new LuggageModel(id, p, v, d, r, c, b, recep);
                        vData.add(luggage);
                    }
                    rep = new LUGAPResponse(LUGAPResponse.LUGGAGE_SUCCESS);
                    rep.setvLuggages(vData);
                    rs.close();
                } 
                catch (SQLException ex) {
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

            private void updateLuggage() 
            {
                if(state != "AUTHENTICATED")
                {
                    rep = new LUGAPResponse(LUGAPResponse.LOGIN_FAILED,"Veuillez vous authentifier d'abord");
                    return;
                }
                try 
                {
                    LuggageModel l = req.luggage;
                    System.out.println(l.getCharge()+l.getDouane()+l.getRemarques()+l.getReceptionne());
                    bd.insertQuery("LOCK TABLE bagages WRITE");
                    bd.insertQuery("UPDATE bagages set receptionne = '"+l.getReceptionne()+"',charge = '"+l.getCharge()+"', douane ='"+l.getDouane()+"', remarques='"+l.getRemarques()+"'where idBagages = '"+l.getIdBaggages()+"' and numeroBillet = '"+l.getNumeroBillet()+"'");
                    bd.insertQuery("UNLOCK TABLE");
                    
                    rs = bd.executeQuery("select * from bagages where numerobillet in (select numerobillet from billets where idvol = (select idVol from billets where numeroBillet = '"+l.getNumeroBillet()+"'))");

                    Vector<LuggageModel> vData = new Vector<LuggageModel>();
                    while(rs.next())
                    {
                        double p;
                        String id,r,recep,d,c,b,v;
                        id = rs.getString("idBagages");
                        r = rs.getString("remarques");
                        recep = rs.getString("receptionne");
                        d = rs.getString("douane");
                        c = rs.getString("charge");
                        b = rs.getString("NumeroBillet");
                        v = rs.getString("valise");
                        p = rs.getDouble("poids");
                        LuggageModel luggage = new LuggageModel(id, p, v, d, r, c, b, recep);
                        vData.add(luggage);
                    }
                    rep = new LUGAPResponse(LUGAPResponse.UPDATE_SUCCESS);
                    rep.setvLuggages(vData);
                }
                catch (SQLException ex) 
                {
                    rep = new LUGAPResponse(LUGAPResponse.UPDATE_FAILED);
                    Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        };
    }
    
}
