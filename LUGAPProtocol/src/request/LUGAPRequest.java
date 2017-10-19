/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolLUGAP;

import java.net.Socket;
import messageinterface.Request;
import utils.ServerConsole;

/**
 *
 * @author Sadik
 */
public class LUGAPRequest implements Request {

    @Override
    public Runnable createRunnable(Socket s, ServerConsole cs) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello !");
            }
        };
    }
    
}
