/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import ConfigurationFile.Configuration;
import Model.Fly;
import database.utilities.BeanBDAccess;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.JOptionPane;
import message.ConfirmPayMessage;
import message.FlyMessage;
import message.HandshakeMessage;
import message.LoginMessage;
import request.TICKMAPRequest;
import response.TICKMAPResponse;

/**
 *
 * @author Sadik
 */
@WebServlet(name = "ServletController", urlPatterns = {"/CaddieVirtuelle"})
public class ServletController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public BeanBDAccess bd;
    private Configuration config;
    
    private PublicKey serverPK;
    private void getKeys()
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try 
        {
            String keystorePassword = config.getPropertie("KEYSTORE_PASS");
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(new FileInputStream("C:\\Users\\Sadik\\Desktop\\InpresAirport\\Resources\\Web.keystore"),keystorePassword.toCharArray());
            webK = (PrivateKey) ks.getKey("webkey", keystorePassword.toCharArray());
            System.out.println("Key chargé");
            X509Certificate cert = (X509Certificate) (ks.getCertificate("servercertificat"));
            serverPK = cert.getPublicKey();
            
        }
        catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException ex) 
        {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }
    @Override
    public void destroy()
    {
        bd.Close();
    }
    @Override
    public void init()
    {
        config = new Configuration("C:\\Users\\Sadik\\Desktop\\InpresAirport\\Resources\\Configuration.properties");
        bd = new BeanBDAccess("MYSQL","bd_airport","root","sadikano","localhost");
        bd.connectDB();
        getKeys();
        System.out.println("Lancement du timer de néttoyage !");
        new Timer().scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    System.out.println("Timer de néttoyage lancé !");
                    ResultSet rs = bd.executeQuery("select * from reservation where SEC_TO_TIME(TIME_TO_SEC(now()) - TIME_TO_SEC(DateReservation)) > 60*2");
                    while(rs.next())
                    {
                        bd.insertQuery("UPDATE vols set PlaceRestante = PlaceRestante + "+rs.getInt("Place")+" where idVol = "+rs.getInt("idVol"));
                        bd.insertQuery("DELETE FROM reservation where idReservation = "+rs.getInt("idReservation"));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
          }, 0, 2*60*1000);
              
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        response.setContentType("text/html");
        ServletContext sc = getServletContext();
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");

        PrintWriter out = response.getWriter();
        if(action!=null)
        {
            System.out.println(action);
            switch(action)
            {
                case "Connexion" : 
                {
                    String username =request.getParameter("username");
                    String pwd = request.getParameter("password");
                    boolean isNew = request.getParameter("newclient")!=null;
                    if(isNew)
                    {
                        try 
                        {
                            ResultSet rs = bd.executeQuery("SELECT * FROM clients where login = '"+username+"'");
                            if(rs.next())
                            {
                                sc.log("Utilisateur "+username+" existe déjà !");
                                request.getRequestDispatcher("index.html").forward(request, response);
                            }
                            else
                            {
                                bd.insertQuery("INSERT INTO clients VALUES ('"+username+"', '"+pwd+"')");
                                sc.log("Utilisateur "+username+" a bien été enregistré !");
                                setConnected(request, username);
                                request.getRequestDispatcher("/WEB-INF/JSPInit.jsp").forward(request, response);
                            }
                        }
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        try 
                        {
                            ResultSet rs = bd.executeQuery("SELECT * from clients where login ='"+username+"' AND password ='"+pwd+"'");
                            if(rs.next())
                            {
                                sc.log("Connexion reussie pour l'utilisateur "+username+" !");
                                setConnected(request, username);
                                request.getRequestDispatcher("/WEB-INF/JSPInit.jsp").forward(request, response);
                            }
                            else
                            {
                                sc.log("Mot de passe ou login incorrecte pour l'utilisateur "+username+" !");
                                request.getRequestDispatcher("index.html").forward(request, response);

                            }
                        } 
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                break;
                case "Caddie" : 
                {
                    if(isConnected(request))
                    {
                        System.out.println("Redirection !");
                        setFlyList(request);
                        request.getRequestDispatcher("/WEB-INF/JSPCaddie.jsp").forward(request, response);
                    }
                    else
                    {
                        System.out.println("Not Connected !");
                        request.getRequestDispatcher("/index.html").forward(request, response);
                    }
                }
                break;
                case "AddToCart" : 
                {
                    if(isConnected(request))
                    {
                        HttpSession session = request.getSession();
                        String login = (String)session.getAttribute("Connected");
                        try 
                        {
                            synchronized(this)
                            {
                                ResultSet rs = bd.executeQuery("SELECT * FROM vols");
                                while(rs.next())
                                {
                                    int idVol = rs.getInt("idVol");
                                    String place = request.getParameter(""+idVol);
                                    if(place == null)
                                        System.out.println("C'est null pour idVol ="+idVol);
                                    else
                                        System.out.println("idVol = "+idVol);
                                    if(place!= null && Integer.parseInt(place) !=0)
                                    {
                                        
                                        System.out.println("PLACE :: "+place);
                                        //reservation
                                        if(rs.getInt("PlaceRestante") - Integer.parseInt(place) >=0)
                                        {
                                            bd.insertQuery("INSERT INTO RESERVATION (`client`, `Place`, `idVol`) VALUES('"+login+"',"+place+","+idVol+")");
                                            bd.insertQuery("UPDATE vols set PlaceRestante = PlaceRestante - "+place+" where idVol = "+idVol+"");
                                        }
                                    }
                                }
                                setReservation(request);
                                request.getRequestDispatcher("/WEB-INF/JSPPay.jsp").forward(request, response);
                            }
                        } 
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                        request.getRequestDispatcher("/index.html").forward(request, response);
                }
                break;
                case "Panier" :
                {
                    if(isConnected(request))
                    {
                        setReservation(request);
                        request.getRequestDispatcher("/WEB-INF/JSPPay.jsp").forward(request, response);
                    }
                    else
                        request.getRequestDispatcher("/index.html").forward(request, response);
                }
                    break;
                case "ConfirmPayement" : 
                {
                    if(isConnected(request))
                    {
                        String bank = request.getParameter("bank");
                        String message= "";
                        HttpSession session = request.getSession();
                        if(bank!= null && !bank.isEmpty())
                        {
                            
                            String login = (String)session.getAttribute("Connected"); 
                            String total = (String) request.getParameter("total");
                            
                            /*ResultSet rs = bd.executeQuery("SELECT * FROM RESERVATION");
                            while(rs.next())
                            {
                            int nbrPlace = rs.getInt("Place");
                            bd.insertQuery("DELETE FROM RESERVATION WHERE idReservation = "+rs.getInt("idReservation"));
                            bd.insertQuery("INSERT INTO facture (login,total) VALUES('"+login+"',"+total+")");
                            }*/
                            login();
                            
                            Cipher cipher;
                            try 
                            {
                                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                                cipher.init(Cipher.ENCRYPT_MODE,serverPK);
                                Mac hmac = Mac.getInstance("HMAC-MD5","BC");
                                hmac.init(authenticationK);
                                hmac.update(bank.getBytes());
                                byte [] hmacBytes = hmac.doFinal();
                                oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_CONFIRMPAY, new ConfirmPayMessage(ConfirmPayMessage.SUCCESS,hmacBytes,cipher.doFinal(bank.getBytes()),login)));
                                TICKMAPResponse rep = (TICKMAPResponse) ois.readObject();
                                if(rep.getCode() == rep.SUCCESS)
                                {
                                    message = "Merci pour votre payement !";
                                }
                                else
                                {
                                    message = "Erreur lors du payement !";
                                }
                            } 
                            catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException ex) 
                            {
                                Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            logout();
                        }
                        else
                        {
                            request.setAttribute("Message", "Erreur lors du payement !");
                        }
                        setFlyList(request);
                        request.setAttribute("Message", message);
                        request.getRequestDispatcher("/WEB-INF/JSPCaddie.jsp").forward(request, response);
                    }
                    else
                        request.getRequestDispatcher("/index.html").forward(request, response);
                }
                break;
            }
        }
        else
            request.getRequestDispatcher("index.html").forward(request, response);
    }
    private void setFlyList(HttpServletRequest request)
    {
        
        try {
            //chercher billet
            ResultSet rs = bd.executeQuery("SELECT * FROM vols where heureDepart > now()");
            Vector<Fly> lFly = new Vector<Fly>();
            while(rs.next())
            {
                lFly.add(new Fly(rs.getInt("idVol"),rs.getString("destination"),rs.getDate("HeureArriveeEventuelle"),new Date(rs.getTimestamp("HeureDepart").getTime()),rs.getInt("PlaceRestante"),rs.getInt("PrixPlace")));
            }
            request.setAttribute("FlyList", lFly);
        } catch (SQLException ex) {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean isConnected(HttpServletRequest request)
    {
        HttpSession session = request.getSession(true);
        return session.getAttribute("Connected")!=null;
    }
    private void setConnected(HttpServletRequest request,String username)
    {
        HttpSession session = request.getSession(true);
        if(username!=null) session.setAttribute("Connected", username);
        else session.removeAttribute("Connected");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void setReservation(HttpServletRequest request) 
    {
        try 
        {
            HttpSession session = request.getSession();
            String login = (String)session.getAttribute("Connected");
            System.out.println(login);
            ResultSet r = bd.executeQuery("SELECT * FROM Reservation where client = '"+login+"'");
            Vector<Fly> vReservation = new Vector<Fly>();
            System.out.println("1");
            while(r.next())
            {
                System.out.println("2");
                ResultSet vols = bd.executeQuery("SELECT * FROM VOLS where idVol ="+r.getInt("idVol"));
                if(vols.next())
                {
                    System.out.println("3");
                    Fly f = new Fly();
                    f.setDestination(vols.getString("Destination"));
                    f.setPlace(r.getInt("Place"));
                    f.setPrix(vols.getInt("PrixPlace"));
                    vReservation.add(f);
                }
            }
            request.setAttribute("ToPay", vReservation);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void logout() 
    {
        if(socket==null || socket.isClosed())
                return;
        try
        {
            oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_LOGOUT));
            TICKMAPResponse rep = (TICKMAPResponse) ois.readObject();
            if(rep.getCode() == TICKMAPResponse.SUCCESS)
                System.out.println("Deconnexion réussie !");
            oos.close();
            ois.close();
            socket.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handshake() 
    {
        try 
        {
            oos.writeObject(new TICKMAPRequest(TICKMAPRequest.REQUEST_WEBHANDSHAKE));
            TICKMAPResponse rep = (TICKMAPResponse) ois.readObject();
            if(rep.isSuccess())
            {
                System.out.println("Handshake ok !");
                //Encrypted secret keys
                byte[] a = ((HandshakeMessage)rep.getMessage()).getAuthenticationK();
                byte[] c = ((HandshakeMessage)rep.getMessage()).getCipherK();
                //Get a Cipher object
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                cipher.init(Cipher.DECRYPT_MODE, webK);
                //Decrypt the keys
                byte[] authKeyBytes = cipher.doFinal(a);
                byte[] cipherKeyBytes = cipher.doFinal(c);
                //Convert Byte keys  into SecretKey
                authenticationK = new SecretKeySpec(authKeyBytes, 0, authKeyBytes.length, "Rijndael");
                cipherK = new SecretKeySpec(cipherKeyBytes, 0, cipherKeyBytes.length, "Rijndael");
                rep = (TICKMAPResponse)ois.readObject();
            }
            else
            {
                logout();
            }
        }
        catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) 
        {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private PrivateKey webK;
    private SecretKey authenticationK;
    private SecretKey cipherK;
    private void login()
    {
        String login="web", password="web";
        String IP = config.getPropertie("BILLETS_IP");
        int port = Integer.parseInt(config.getPropertie("PORT_BILLETS"));
        try 
        {
                System.out.println(password);
                Socket socket = new Socket(IP,port);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                long time = (new Date()).getTime();
                double r = Math.random();
                
                byte[] msgD = buildDigest(time,r,password,login);
                System.out.println("l = "+login+" r = "+r+" time = "+time+"");
                TICKMAPRequest req = new TICKMAPRequest(TICKMAPRequest.REQUEST_LOGIN,new LoginMessage(r,msgD,login,time));
                oos.writeObject(req);
                TICKMAPResponse rep = (TICKMAPResponse)ois.readObject();
                if(rep.getCode() == TICKMAPResponse.SUCCESS)
                {
                    System.out.println("Login billets ok !");
                    handshake();
                }
                else
                {
                    System.out.println("Login billes notok !");
                }

        } 
        catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }
    
    public byte[] buildDigest(long time,double r,String password,String login)
    {
        byte[] msgD = null;
        try 
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1", "BC");
            md.update(login.getBytes());
            md.update(password.getBytes());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream bdos = new DataOutputStream(baos);
            bdos.writeLong(time);
            bdos.writeDouble(r);
            
            md.update(baos.toByteArray());
            msgD = md.digest();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msgD;
    }
}
