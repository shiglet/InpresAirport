<%-- 
    Document   : JSPPay
    Created on : 15-nov.-2017, 20:47:03
    Author     : Sadik
--%>

<%@page import="java.util.Vector"%>
<%@page import="Model.Fly"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Payement</title>
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link rel="stylesheet" href="css/font-awesome.min.css">
        <script src="js/jquery-3.2.1.min.js"></script>
        <script src="js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="jumbotron text-center">
            <h1><i class="glyphicon glyphicon-plane"></i> Inpres Airport</h1>
            <p>Panier</p>
        </div>
        <%
            Vector<Fly> vReservation = (Vector<Fly>)request.getAttribute("ToPay");
            int total =0;
            if(vReservation!=null && vReservation.size()>0)
            {
            %>
            <table class="table" border = "1">
                <tr>
                    <caption>Panier</caption>
                    <td class="text-primary">Destination</td>
                    <td class="text-primary">Place Reservée</td>
                    <td class="text-primary">Prix par place</td>
                    <td class="text-primary">Total</td>
                </tr>
            <%
            for(Fly f : vReservation)
            {
                %>
                <tr>
                   <td><%= f.getDestination()%></td>
                   <td><%= f.getPlace()%></td>
                   <td><%= f.getPrix()%></td>
                   <td><%=f.getPrix()*f.getPlace() %></td>
                </tr>
                <%
                total+=f.getPrix()*f.getPlace();
            }
        %>
        <tr>
            <td></td>
            <td></td>
            <td></td>
            <td class="text-primary"><%=total%>€</td>
        </tr>
    </table>
    <p>Total à payer = <%=total%> €</p>
    <form action="controller" method="GET">
        <p>Numero de Compte bancaire : <input type="text" name="bank"/></p>
        <input type="submit" class="btn btn-success" value="Payer" />
        <input type="hidden" value="ConfirmPayement" name="action"/>
        <input type="hidden" value="<%=total%>" name="total"/>
    </form>
        <%}
        else
        {
            %>
            <p class="text-danger">Vous n'avez rien à payer</p>
            <%
        }
        %>
        <form method="GET" action="controller">
            <div class="text-right"><input type="submit" class="btn btn-danger" value="Retour" /></div>
            <input type="hidden" value="Caddie" name="action"/>
        </form>
    </body>
</html>
