/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.utilities;

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
    
    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private ResultSet rs;
    private String type;
    private String urlDB;
    private String sampleProperty;
    private String user;
    private String password;
    private Connection con;
    private PropertyChangeSupport propertySupport;
    
    public BeanBDAccess() {
        propertySupport = new PropertyChangeSupport(this);
    }
    public BeanBDAccess(String t, String u, String us , String p) {
        propertySupport = new PropertyChangeSupport(this);
        type = t;
        user=us;
        urlDB = u;
        password = p;
    }

    public String getSampleProperty() {
        return sampleProperty;
    }
    
    public void setSampleProperty(String value) {
        String oldValue = sampleProperty;
        sampleProperty = value;
        propertySupport.firePropertyChange(PROP_SAMPLE_PROPERTY, oldValue, sampleProperty);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    public boolean connectDB()
    {
        boolean ret=true;
        try
        {
            if(type == "MYSQL")
            {
                Class.forName("com.mysql.jdbc.Driver");
                urlDB = "jdbc:mysql://localhost:3306/"+urlDB;
                System.out.println("Driver MYSQL chargé");
            }
            else if(type=="ORACLE")
            {
                Class.forName("oracle.jdbc.OracleDriver");
                urlDB = "jdbc:oracle:thin:@localhost:1521/"+urlDB;
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
    public synchronized ResultSet executeQuery(String query) throws SQLException
    {
        java.sql.Statement instruc = null;
        try {
            instruc = con.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(BeanBDAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Création d'une instance d'instruction pour cette connexion");
        rs = instruc.executeQuery(query);
        return rs;
    }
    
    public synchronized void insertQuery(String query)
    {
      try
      {
        java.sql.Statement instruc = con.createStatement();
        instruc.executeUpdate(query);
        System.out.println("L'execute OK");
      }
      catch (SQLException ex) {
                  System.out.println("L'execute NOK");
            Logger.getLogger(BeanBDAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
