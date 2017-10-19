/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messageinterface;

import java.net.Socket;
import utils.ServerConsole;

/**
 *
 * @author Sadik
 */
public interface Request 
{
    public Runnable createRunnable (Socket s,ServerConsole cs);
}
