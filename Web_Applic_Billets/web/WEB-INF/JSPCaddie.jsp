<%-- 
    Document   : JSPCaddie
    Created on : 15-nov.-2017, 20:46:55
    Author     : Sadik
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Vector"%>
<%@page import="Model.Fly"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Caddie Virtuelle - Inpres Airport</title>
    </head>
    <body>
        <h1>Voici les vols disponibles : </h1>
         
    <form  name="Acheter" action="controller" method='GET'>
        <table border = "1"><tr>
            <caption>Liste des vols</caption>
                <td>Destination</td>
                <td>Date départ</td>
                <td>Places disponibles</td>
                <td>Prix par place</td>
                <td>Reserver</td>
            </tr>
        <%
        String message = (String)request.getAttribute("Message");
        if(message!=null)
        {
            %>
            <p><%=message%><br/><br/></p>
            <%
        }
        Vector<Fly> lFly =(Vector<Fly>) request.getAttribute("FlyList");
        if(lFly != null)
        {        for(Fly f : lFly)
        {
            %>
            
            <tr>
                   <td><%= f.getDestination()%></td>
                   <td><%= new SimpleDateFormat("dd-MMM-yy hh.mm").format(f.getDepart())%></td>
                   <td><%= f.getPlace()%></td>
                   <td><%= f.getPrix()%>€</td>
                   <td><label><input type="text" value="0" name="<%= f.getIdVol()%>"/></label></td>
            </tr>
        <%}
        %>
        </table>
        <input type="submit" value="Ajouter au Caddie" name="ajouter"/>
        <input type="hidden" value="AddToCart" name="action"/>
        </form>
        <form action="controller" method="GET">
        <input type="submit" value="Panier" />
        <input type="hidden" value="Panier" name="action"/>
    </form>
        <%}
    else
    {
        %>
        <p>Aucun vol disponible</p>
        <%
    }%>
    </body>
</html>
