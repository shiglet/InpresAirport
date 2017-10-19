/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import database.utilities.BeanBDAccess;
import java.net.Socket;
import interfaces.Request;
import interfaces.ServerConsole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadik
 */
public class LUGAPRequest implements Request {
    public static final short REQUEST_LOGIN = 1;
    public static final short REQUEST_LOGOUT = 2;
    private short type;
    private String data;
    public LUGAPRequest(short t,String d)
    {
        type = t;
        data = d;
    }
    
    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs) {
        return new Runnable() {
            @Override
            public void run() {
                BeanBDAccess bd = new BeanBDAccess("MYSQL","bd_airport","root","sadikano");
                while(type != 1)
                {
                    switch(type)
                    {
                        case REQUEST_LOGIN :
                            String[] tokens = data.split("$");
                            ResultSet rs = bd.executeQuery("select password from agents where login ='"+tokens[0]+"'");
                    
                        try 
                        {
                            if(rs.next())
                            {
                                String pass = rs.getString("password");
                                cs.trace("server#"+tokens[0]+"/"+pass);
                                if(pass.equals(tokens[1]))
                                    cs.trace("server#login ok");
                                else
                                    cs.trace("server#login not ok");
                            }
                                break;
                        } catch (SQLException ex) {
                            Logger.getLogger(LUGAPRequest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        case REQUEST_LOGOUT : 
                            break;
                    }
                }
            }
        };
    }
    
}
