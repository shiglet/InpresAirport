/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import database.utilities.BeanBDAccess;
import interfaces.Request;
import interfaces.ServerConsole;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Sadik
 */
public class TICKAMPRequest implements Request
{

    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs, ObjectOutputStream oos, ObjectInputStream ois, BeanBDAccess beanBD) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
