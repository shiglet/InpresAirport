<%-- 
    Document   : JSPInit
    Created on : 15-nov.-2017, 20:46:41
    Author     : Sadik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Initialisation du caddie</title>
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link rel="stylesheet" href="css/font-awesome.min.css">
        <script src="js/jquery-3.2.1.min.js"></script>
        <script src="js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="jumbotron text-center">
            <h1><i class="glyphicon glyphicon-plane"></i> Inpres Airport</h1>
        </div>
        <div class="container">
            <div class="panel-body">
            <h1>BONJOUR => INITIALISATION DU CADDIE...</h1>
            <form action="controller">
                <span class="text-right"><input type="submit" value="Vers caddie" class="btn btn-success" /></span>
                <input type="hidden" value="Caddie" name="action"/>
            </form>
            </div>
        </div>
    </body>
</html>
