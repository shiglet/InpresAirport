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
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link rel="stylesheet" href="css/font-awesome.min.css">
        <script src="js/jquery-3.2.1.min.js"></script>
        <script src="js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="jumbotron text-center">
            <h1><i class="glyphicon glyphicon-plane"></i> Inpres Airport</h1>
            <p>Caddie virtuelle</p>
        </div>
        <h1>Voici les vols disponibles : </h1>
         
    <form  name="Acheter" action="controller" method='GET'>
        <table class="table" border = "1"><tr>
            <caption>Liste des vols</caption>
                <td class="text-primary">Destination</td>
                <td class="text-primary">Date départ</td>
                <td class="text-primary">Places disponibles</td>
                <td class="text-primary">Prix par place</td>
                <td class="text-primary">Reserver</td>
            </tr>
        <%
        String message = (String)request.getAttribute("Message");
        if(message!=null)
        {
            %>
        <div class="panel panel-warning">
            <div class="panel-heading">Message</div>
            <div class="panel-body"><%=message%></div>
        </div>
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
        <br/>
        <input type="submit" class="btn btn-success" value="Ajouter au Caddie" name="ajouter"/>
        <input type="hidden" value="AddToCart" name="action"/>
        </form>
        <br/>
        <form action="controller" method="GET">
            <div class="text-right"><input type="submit" class="btn btn-danger" value="Panier" /></div>
        <input type="hidden" value="Panier" name="action"/>
    </form>
        <%}
    else
    {
        %>
        <p class="text-danger">Aucun vol disponible</p>
        <%
    }%>
    </body>
</html>
