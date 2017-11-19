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
    </head>
    <body>
        <h1>Payement</h1>
        <%
            Vector<Fly> vReservation = (Vector<Fly>)request.getAttribute("ToPay");
            int total =0;
            if(vReservation!=null && vReservation.size()>0)
            {
            %>
            <table border = "1">
                <tr>
                    <caption>Panier</caption>
                    <td>Destination</td>
                    <td>Place Reservée</td>
                    <td>Prix par place</td>
                    <td>Total</td>
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
            <td><td>
            <td><%=total%></td>
        </tr>
    </table>
    <p>Total à payer = <%=total%> €</p>
    <form action="controller" method="GET">
        <p>Numero de Compte bancaire : <input type="text" name="bank"/></p>
        <input type="submit" value="Payer" />
        <input type="hidden" value="ConfirmPayement" name="action"/>
    </form>
        <%}
        else
        {
            %>
            <p>Vous n'avez rien à payer</p>
            <%
        }
        %>
        <form method="GET" action="controller">
            <input type="submit" value="Retour" />
            <input type="hidden" value="Caddie" name="action"/>
        </form>
    </body>
</html>
