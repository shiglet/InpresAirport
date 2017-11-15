/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.utilities;

import ConfigurationFile.Configuration;
import java.beans.*;
import java.io.Serializable;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Sadik
 */
public class BeanBDAccess implements Serializable {
    private ResultSet rs;
    private String type;
    private String urlDB;
    private String user;
    private String password;
    private Connection con;
    private String ip;
    public void Close()
    {
        try
        {
        if(con != null) con.close();
        }
        catch (SQLException ex) {
                  System.out.println("close NOK");
            Logger.getLogger(BeanBDAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public BeanBDAccess(String t, String u, String us , String p) {
        type = t;
        user=us;
        urlDB = u;
        password = p;
        Configuration cfg = new Configuration();
        ip = cfg.getPropertie("DATABASE_IP");
    }
    public BeanBDAccess(String t, String u, String us , String p,String i) {
        type = t;
        user=us;
        urlDB = u;
        password = p;
        Configuration cfg = new Configuration();
        ip = i;
    }

    public boolean connectDB()
    {
        boolean ret=true;
        try
        {
            if(type == "MYSQL")
            {
                Class.forName("com.mysql.jdbc.Driver");
                urlDB = "jdbc:mysql://"+ip+":3306/"+urlDB;
                System.out.println("Driver MYSQL chargé");
            }
            else if(type=="ORACLE")
            {
                Class.forName("oracle.jdbc.OracleDriver");
                urlDB = "jdbc:oracle:thin:@"+ip+":1521:XE";
                System.out.println("Driver ORACLE chargé");
            }

        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Driver adéquat non trouvable : " + e.getMessage());
            ret=false;
        }
        try
        {
            con = DriverManager.getConnection(urlDB,user,password);
            System.out.println("Connexion à la BDD réalisée");
        }
        catch (SQLException e)
        {
            System.out.println("Erreur SQL : " + e.getMessage());
            ret=false;
        }
        return ret;
    }
    
    public synchronized ResultSet executeQuery(String query)
    {
        java.sql.Statement instruc = null;
        try {
            instruc = con.createStatement();
            System.out.println("Création d'une instance d'instruction pour cette connexion");
            rs = instruc.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(BeanBDAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public synchronized void insertQuery(String query) throws SQLException
    {
        java.sql.Statement instruc = con.createStatement();
        instruc.executeUpdate(query);
    }
}
