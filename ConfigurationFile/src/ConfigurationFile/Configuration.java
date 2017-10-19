package ConfigurationFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sadik
 */
public class Configuration 
{
    private Properties properties = new Properties();
    public Configuration()
    {
        try 
        {
            File f = new File(".");
            System.out.println(f.getAbsoluteFile());
            properties.load(new FileInputStream(".."+ File.separator +"Resources"+File.separator+"Configuration.properties"));
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Configuration(String path)
    {
        try 
        {
            properties.load(new FileInputStream(path));
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String getPropertie(String propertieName)
    {
        return properties.getProperty(propertieName.toUpperCase());
    }
}
