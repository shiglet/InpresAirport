/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import ConfigurationFile.Configuration;
import database.utilities.BeanBDAccess;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
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
    private BeanBDAccess bd;
    @Override
    public void init(ServletConfig servletconfig)
    {
        System.out.println("SISISIIS");
        bd = new BeanBDAccess("MYSQL","bd_airport","root","sadikano","localhost");
        bd.connectDB();
        if(bd!= null )
            System.out.println("Non null");
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        if(action!=null)
        {
            if(!isConnected(request))
            {
                if(action.equals("Connexion"))
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
                                request.getRequestDispatcher("index.html").forward(request, response);
                            }
                            else
                            {
                                bd.insertQuery("INSERT INTO clients VALUES ('"+username+"', '"+pwd+"')");
                            }
                        }
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(ServletController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        
                    }
                }
            }
            else
            {
                
            }
            
        }
        else
            request.getRequestDispatcher("index.html").forward(request, response);
    }
    private boolean isConnected(HttpServletRequest request)
    {
        HttpSession session = request.getSession(true);
        return session.getAttribute("Connected")!=null;
    }
    private void setConnected(HttpServletRequest request,boolean b)
    {
        HttpSession session = request.getSession(true);
        if(b) session.setAttribute("Connected", "Ok");
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

}
