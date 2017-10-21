/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testapp;

import request.LUGAPRequest;

/**
 *
 * @author Sadik
 */
public class TestApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        LUGAPRequest l = new LUGAPRequest(LUGAPRequest.REQUEST_LOGIN, "hi");
        System.out.println(LUGAPRequest.REQUEST_LOGIN);
    }
    
}
