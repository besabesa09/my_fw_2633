package p16.controller;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String url = req.getRequestURI();

    // Définir le type de contenu de la réponse
    resp.setContentType("text/html");

    // Obtenir le PrintWriter pour écrire dans la réponse
    PrintWriter aff = resp.getWriter();

    // Écrire le message dans la réponse
    aff.println("Bienvenue dans le test de Sprint 0. URL : " + url);
    }
}
