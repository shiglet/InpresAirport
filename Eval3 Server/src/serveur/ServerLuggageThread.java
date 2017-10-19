/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import interfaces.Request;
import interfaces.ServerConsole;
import utils.TasksSource;
/**
 *
 * @author Sadik
 */
public class ServerLuggageThread extends Thread 
{
    private int port;
    private ServerSocket SSocket = null;
    private ServerConsole app;
    private Runnable currentTask;
    private TasksSource tasks;
    private Socket CSocket = null;
    private int nbrClient;
    private boolean fin;
    private int waitingTasks;
    public ServerLuggageThread(int p,ServerConsole sc, TasksSource ts)
    {
        port = p; 
        app = sc;
        nbrClient = 3;
        waitingTasks = 0;
        tasks = ts;
    }
    
    public void run()
    {
        try
        {
            SSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            System.err.println("Erreur lors de la création de la ServerSocket : " + e.getMessage()); 
            System.exit(1);
        }
        
        //Starting threads
        for (int i=0; i < nbrClient; i++)
        {
             ClientThread t = new ClientThread(tasks,"Thread numero : "+String.valueOf(i));
             t.start();
        }
        CSocket = null;
        while (!isInterrupted())
        {
            try
            {
                app.trace("Serveur#Serveur en attente");
                CSocket = SSocket.accept();
                app.trace(CSocket.getRemoteSocketAddress().toString()+"#Client connecté");
            } 
            catch (IOException ex) 
            {
                System.err.println("Erreur lors du accept" + ex.getMessage());
                System.exit(1);
            }
            ObjectInputStream ois=null;
            ObjectOutputStream oos=null;
            Request req = null;
            try
            {
                oos = new ObjectOutputStream(CSocket.getOutputStream());
                ois = new ObjectInputStream(CSocket.getInputStream());
                req = (Request)ois.readObject();
                app.trace("Serveur#Requete lue par le serveur, instance de " +
                req.getClass().getName());
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Erreur de def de classe [" + e.getMessage() + "]");
            }
            catch (IOException e)
            {
                System.err.println("Erreur ? [" + e.getMessage() + "]");
            }
            Runnable travail = req.createRunnable(CSocket, app);
            if (travail != null)
            {
                tasks.recordTache(travail);
                app.trace("Serveur#Travail mis dans la file");
            }
            else app.trace("Serveur#Travail non mis dans la file");
        }
    }
}
