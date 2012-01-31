

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <% String recipient = request.getParameter("recipient");
           String subject = request.getParameter("subject") ;
           String bodyOfMessage = request.getParameter("bodyOfMessage");
           //out.print("Forwarding");
           RequestDispatcher view = request.getRequestDispatcher("sendmail.do");
           view.forward(request, response);
        %>
        <h1>Hello World!</h1>
    </body>
</html>
