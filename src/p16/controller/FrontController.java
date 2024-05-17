package p16.controller;

import p16.annotation.Controller;
import p16.model.ScanController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FrontController extends HttpServlet {

    private ArrayList<Class<?>> controllers;
    private static boolean scanned = false;

    // getter et setter
    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();

        // Récupérer la valeur de l'init-param package_name
        String packageName = this.getInitParameter("package_name");

        // Vérifier si les contrôleurs ont déjà été scannés
        if (!scanned) {
            // Scanner les contrôleurs et stocker la liste dans l'attribut
            try {
                this.setControllers(ScanController.goScan(packageName));
                scanned = true;
            } catch (ClassNotFoundException | IOException e) {
                throw new ServletException("Erreur lors du scan des contrôleurs", e);
            }
        }
        resp.setContentType("text/html");
        // Afficher la liste des contrôleurs
        PrintWriter aff = resp.getWriter();
        aff.println("<h2>test de Sprint 0.<br> URL : </h2>" + url + "<br>");
        aff.println("<h2>Test sprint 1 </h2><br>");
        aff.println("Liste des controllers :");
        for (Class<?> controller : this.getControllers()) {
            aff.println(controller.getName());
        }
    }
}
