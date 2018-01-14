/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication8;

import ui.Application_Billets;
import ui.ServeurBilletsUI;

/**
 *
 * @author Sadik
 */
public class JavaApplication8 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        /* Create and display the form */
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                new Application_Billets().setVisible(true);
            }
        });
        t1.start();
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                new ServeurBilletsUI().setVisible(true);
            }
        } );
        t2.start();
    }
    
}
