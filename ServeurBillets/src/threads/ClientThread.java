/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import interfaces.TasksSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gaffe
 */
public class ClientThread extends Thread
{
    private Runnable tacheEnCours;
    private TasksSource tasks;
    private String name;
    private boolean fin;
    public ClientThread(TasksSource ts, String n)
    {
        tasks = ts;
        name = n;
    }
    
    public void run()
    {
        
        while(!isInterrupted())
        {
            try
            {
                tacheEnCours = tasks.getTache();
            }
            catch (InterruptedException e)
            {
                System.out.println("Interruption : " + e.getMessage());
            }
            tacheEnCours.run();
        }
    }
}
