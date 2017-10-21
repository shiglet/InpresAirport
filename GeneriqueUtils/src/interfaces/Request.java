/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import database.utilities.BeanBDAccess;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Sadik
 */
public interface Request 
{
    public Runnable createRunnable (Socket s,ServerConsole cs,ObjectOutputStream oos, ObjectInputStream ois,BeanBDAccess beanBD);
}
