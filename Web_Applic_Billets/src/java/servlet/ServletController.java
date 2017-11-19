/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Model.Fly;
import database.utilities.BeanBDAccess;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    
    @Override
    public void destroy()
    {
        bd.Close();
    }
    @Override
    public void init()
    {
        bd = new BeanBDAccess("MYSQL","bd_airport","root","sadikano","localhost");
        bd.connectDB();
        System.out.println("Lancement du timer de néttoyage !");
        new Timer().scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    System.out.println("Timer de néttoyage lancé !");
                    ResultSet rs = bd.executeQuery("select * from reservation where SEC_TO_TIME(TIME_TO_SEC(now()) - TIME_TO_SEC(DateReservation)) > 60*5");
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
                        setFlyList(request);
                        request.getRequestDispatcher("/WEB-INF/JSPCaddie.jsp").forward(request, response);
                    }
                    else
                        request.getRequestDispatcher("/index.html").forward(request, response);
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
                                    if(place!= null && Integer.parseInt(place) !=0)
                                    {
                                        //reservation
                                        if(rs.getInt("PlaceRestante") - Integer.parseInt(place) >=0)
                                        {
                                            bd.insertQuery("INSERT INTO RESERVATION (`client`, `Place`, `idVol`) VALUES('"+login+"',"+place+","+idVol+")");
                                            bd.insertQuery("UPDATE vols set PlaceRestante = PlaceRestante - "+place+" where idVol = "+idVol+"");
                                            setReservation(request);
                                            request.getRequestDispatcher("/WEB-INF/JSPPay.jsp").forward(request, response);
                                            break;
                                        }
                                        else
                                        {
                                            //pas assez de place !
                                            request.setAttribute("Message", "Pas assez de place restante pour le vol souhaité. Réessayez...");
                                            setFlyList(request);
                                            request.getRequestDispatcher("/WEB-INF/JSPCaddie.jsp").forward(request, response);
                                            break;
                                        }
                                    }
                                }
                                setFlyList(request);
                                request.getRequestDispatcher("/WEB-INF/JSPCaddie.jsp").forward(request, response);
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
                            try 
                            {
                                String login = (String)session.getAttribute("Connected");
                                ResultSet rs = bd.executeQuery("SELECT * FROM RESERVATION");
                                while(rs.next())
                                {
                                    int nbrPlace = rs.getInt("Place");
                                    bd.insertQuery("INSERT INTO Billets (login,NombrePassager, idVol) VALUES('"+login+"',"+nbrPlace+","+rs.getInt("idVol")+")");
                                    bd.insertQuery("DELETE FROM RESERVATION WHERE idReservation = "+rs.getInt("idReservation"));
                                }
                                message = "Merci pour votre payement !";
                            } catch (SQLException ex) 
                            {
                                request.setAttribute("Message", "Erreur lors du payement !");
                                Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                            }
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
            ResultSet rs = bd.executeQuery("SELECT * FROM vols");
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
}
