/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author Sadik
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        byte[] x = "salut ça va ?".getBytes();
        String s = new String(x);
        String lala = "sisi"+"$"+"plop"+"$"+s+"$weshalors";
        byte[] jiji = lala.trim().getBytes();
        StringBuffer message=new StringBuffer();
        for(int i = 0;i<jiji.length;i++)
        {
            message.append((char)jiji[i]);
        }
        String am = message.toString().trim().split("\\$")[2];
        byte[] b =  am.getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(int i = 0;i<jiji.length;i++)
        {
            out.write(jiji[i]);
        }
        String recon = new String(out.toByteArray()).trim().split("\\$")[2];
        byte[] finalBytes = recon.getBytes();
        if(Arrays.equals(x,finalBytes))
            System.out.println("YES !");
    }
    
}
