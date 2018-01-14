/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 * @author Sadik
 */
public class ThreadReception extends Thread{
    private String nom;
    private MulticastSocket socketGroupe;
    private JList LMsgRecus;
    private static int JOIN =1;
    private static int POST_QUESTION =2;
    private static int ANSWER_QUESTION =3;
    private static int POST_EVENT =4;
    private static int LEAVE =5;
    private String sep;
    public ThreadReception(String n , MulticastSocket ms,JList l,String sep)
    {
        nom = n;
        socketGroupe = ms;
        LMsgRecus = l;
        this.sep = sep;
    }
    public void run()
    {
        boolean enMarche = true;
        System.out.println(nom+">>Demarre");
        while(enMarche)
        {
            try
            {
                byte[] buf = new byte[1000];
                
                DatagramPacket dtg = new DatagramPacket(buf, buf.length);
                socketGroupe.receive(dtg);
                //To do ... Update de la jtable
                String message = new String (buf).trim();
                    //message = message.substring(0, message.length()-1);
                    System.out.println("Message reçu : "+message);
                    String[] messageSplit = message.split("\\"+sep);
                    
                    int type = Integer.parseInt(messageSplit[0]);
                    DefaultListModel dlm=null ;
                    if(type == JOIN)
                    {
                        dlm = (DefaultListModel) LMsgRecus.getModel();
                        dlm.addElement("JOIN#"+messageSplit[2]+" : "+messageSplit[3]);
                        LMsgRecus.setModel(dlm);
                    }
                    else if(type==POST_QUESTION)
                    {
                        int r = Integer.parseInt(messageSplit[5]);
                        System.out.println(r);
                        if(hashage(messageSplit[3],r) == Integer.parseInt(messageSplit[4]))
                        {
                            System.out.println("Les digests correspondent --> ajout à la JList");
                            dlm = (DefaultListModel) LMsgRecus.getModel();
                            dlm.addElement(messageSplit[1]+"#"+messageSplit[2]+" : "+messageSplit[3]);
                            LMsgRecus.setModel(dlm);
                        }
                    }
                    else if(type == ANSWER_QUESTION)
                    {
                        dlm = (DefaultListModel) LMsgRecus.getModel();
                        dlm.addElement("To "+messageSplit[1]+"#"+messageSplit[2]+" : "+messageSplit[3]);
                        LMsgRecus.setModel(dlm);
                    }
                    else if(type == POST_EVENT)
                    {
                        dlm = (DefaultListModel) LMsgRecus.getModel();
                        dlm.addElement(messageSplit[1]+"#"+messageSplit[2]+" : "+messageSplit[3]);
                        LMsgRecus.setModel(dlm);
                    }
                    else if(type == LEAVE)
                    {
                        dlm = (DefaultListModel) LMsgRecus.getModel();
                        dlm.addElement("LEAVE#"+messageSplit[2]+" : "+messageSplit[3]);
                        LMsgRecus.setModel(dlm);
                    }
                
            } 
            catch (IOException ex) {
                Logger.getLogger(ThreadReception.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(NumberFormatException ex)
            {
                System.out.println("Message reçu dans un format incorrecte !");
            }
        }
    }
    public int hashage(String s,int r)
    {
        int sum=0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            sum = sum + (int) c;
        }
        sum = (sum + r % 67)*r;
        return sum;
    }
}
