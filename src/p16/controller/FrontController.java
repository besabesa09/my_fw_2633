package p16.controller;

import p16.exception.DuplicateUrlException;
import p16.exception.NoPackageException;
import p16.model.InitController;
import p16.model.Mapping;
import p16.model.ProcessController;
import p16.model.TraiteRequest;
import p16.model.VerbAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
public class FrontController extends HttpServlet {
    private HashMap<String, Mapping> urlMappings = new HashMap<>();
    private ArrayList<Class<?>> controllers;
    private ProcessController processController;

    public HashMap<String, Mapping> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(HashMap<String, Mapping> urlMappings) {
        this.urlMappings = urlMappings;
    }

    // getter et setter
    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            String packageName = this.getInitParameter("package_name");
            InitController initController = new InitController();
            urlMappings = initController.initialize(packageName);
            processController = new ProcessController(urlMappings);
        } catch (ClassNotFoundException | IOException | DuplicateUrlException | NoPackageException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter aff = resp.getWriter();
        String url = req.getServletPath();

        try {
            Mapping mapping = processController.getMappingForUrl(url);
            if (mapping == null) {
                processController.sendErrorResponse(resp, aff, "404 Not Found: URL indisponible");
                return;
            }

            String verb = req.getMethod();
            VerbAction verbAction = processController.getVerbActionForRequest(mapping, verb);
            if (verbAction == null) {
                processController.sendErrorResponse(resp, aff,
                        "Erreur: Le verbe " + verb + " n'est pas supporté pour cette URL.");
                return;
            }

            // Récupérer la classe du contrôleur
            Class<?> controllerClass = Class.forName(mapping.getClassName());

            // Récupérer la méthode du contrôleur
            Method controllerMethod = controllerClass.getMethod(verbAction.getMethod(), TraiteRequest.getParameterTypes(controllerClass, verbAction.getMethod()));

            Object result = processController.invokeControllerMethod(mapping, verbAction, req, resp);
            
            processController.handleResponse(result, req, resp, aff, controllerMethod);
        } catch (Exception e) {
            String out = e.getLocalizedMessage() +"\n"+ e.getMessage() + "\n" + e.getStackTrace();
            processController.sendErrorResponse(resp, aff, out);
        }
    }
}
