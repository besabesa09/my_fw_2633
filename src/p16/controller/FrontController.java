package p16.controller;

import p16.annotation.Controller;
import p16.annotation.Get;
import p16.model.Mapping;
import p16.model.ModelView;
import p16.model.ScanController;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FrontController extends HttpServlet {
    private HashMap<String, Mapping> urlMappings = new HashMap<>();
    private ArrayList<Class<?>> controllers;

    // getter et setter
    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }

    @Override
    public void init() throws ServletException{
        super.init();

        // récupérer la liste des contrôleurs
        String packageName = this.getInitParameter("package_name");
        try {
            this.setControllers(ScanController.allClasses(packageName));

            // itérer les contrôleurs et récupérer les méthodes annotées par @Get
            for (Class<?> controller : this.getControllers()) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                       //nom_classe et nom_methode
                        String className = controller.getName();
                        String methodName = method.getName();

                        //@Get value
                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value();

                        Mapping mapping = new Mapping(className, methodName);
                        urlMappings.put(url, mapping); //add dans HashMap
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
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
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getServletPath();
    
        // Check URL
        Mapping mapping = urlMappings.get(url);
        if (mapping == null) {
            // L'URL n'est pas dans le mapping, afficher un message d'erreur
            resp.setContentType("text/html");
            PrintWriter aff = resp.getWriter();
            aff.println("<h2>Erreur: L'URL demandée n'est pas disponible!</h2>");
            return;
        }
    
        // Récupération nom_contrôleur et méthode
        String controllerName = mapping.getClassName();
        String methodName = mapping.getMethodName();
    
         // Créer une instance du contrôleur
         try {
            Class<?> controllerClass = Class.forName(controllerName);
            Object controllerInstance = controllerClass.newInstance();
 
            // Récuperation de la méthode à appeler
            Method method = controllerClass.getMethod(methodName);
 
            // Appel de la méthode
            Object result = method.invoke(controllerInstance);
           
            if (result instanceof ModelView) {
            ModelView modelView = (ModelView) result;
            String viewUrl = modelView.getUrl();
            HashMap<String, Object> data = modelView.getData();

            // Définir les données en tant qu'attributs de requête
            for (String key : data.keySet()) {
                req.setAttribute(key, data.get(key));
            }

            RequestDispatcher dispat = req.getRequestDispatcher(viewUrl);
            dispat.forward(req,resp);

        } else { // Si le résultat n'est pas une instance de ModelView, utiliser le code existant
            resp.setContentType("text/html");
            PrintWriter aff = resp.getWriter();
            aff.println("<h2>Test sprint 3 </h2>");
            aff.println("<p><strong>Contrôleur</strong> : " + controllerName + "</p>");
            aff.println("<p><strong>Méthode</strong> : " + methodName + "</p>");
            aff.println("<p><strong>Execution de la fonction "+ methodName +" :</strong></p>");
            aff.println(result.toString());
        }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }    
}
