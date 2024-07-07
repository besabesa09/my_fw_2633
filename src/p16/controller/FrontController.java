package p16.controller;

import p16.annotation.Controller;
import p16.annotation.Get;
import p16.exception.DuplicateUrlException;
import p16.exception.NoPackageException;
import p16.exception.TypeException;
import p16.exception.UrlException;
import p16.model.Mapping;
import p16.model.ModelView;
import p16.model.ScanController;
import p16.model.TraiteRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        try {
        // récupérer la liste des contrôleurs
        String packageName = this.getInitParameter("package_name");
        if (packageName == null || packageName.isEmpty()) {
            // Le package_name est null ou vide, lancer une exception NoPackageException
            throw new NoPackageException("Erreur: Le package_name est requis et ne peut pas être vide");
        }
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

                        if (urlMappings.containsKey(url)) {
                        // L'URL est en double, lancer une exception DuplicateUrlException
                            throw new DuplicateUrlException("Erreur: L'URL " + url + " est déjà utilisée");
                        }

                        Mapping mapping = new Mapping(className, methodName);
                        urlMappings.put(url, mapping); //add dans HashMap
                    }
                }
            }
            
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

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        PrintWriter aff = resp.getWriter();
        String url = req.getServletPath();
        try {
        // Check URL
        Mapping mapping;
            mapping = urlMappings.get(url);
            if (mapping == null) {
                // L'URL n'est pas dans le mapping, afficher un message d'erreur
                resp.setContentType("text/html");
                throw new UrlException("404 Not Found . Url indisponible");
            }

        // Récupération nom_contrôleur et méthode
        String controllerName = mapping.getClassName();
        String methodName = mapping.getMethodName();
        // Créer une instance du contrôleur
        Class<?> controllerClass = Class.forName(controllerName);
        Object controllerInstance = controllerClass.newInstance();
        // Récuperation de la méthode à appeler
        Method method = null;
        for (Method m : controllerClass.getMethods()){
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new NoSuchMethodException(controllerClass.getName() + "." + methodName + "()");
        }
        Object result;
        // Vérifier si la méthode possède des paramètres
        if (method.getParameterCount() > 0) {
            List<Object> parameterValues = TraiteRequest.getParameterValuesCombined(method, req);
                if (parameterValues.size() != method.getParameterCount()) {
                    throw new IllegalArgumentException("nombre de paramètres envoyés différents des paramètres de la méthode");
                }
            result = method.invoke(controllerInstance, parameterValues.toArray());
        } else {
            result = method.invoke(controllerInstance);
        }
            // Vérifier le type d'objet retourné
            if (result instanceof String || result instanceof ModelView) {
                // Le type d'objet retourné est valide, continuer le traitement
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
                    aff.println("<h2>Test sprint 3 </h2>");
                    aff.println("<p><strong>Contrôleur</strong> : " + controllerName + "</p>");
                    aff.println("<p><strong>Méthode</strong> : " + methodName + "</p>");
                    aff.println("<p><strong>Execution de la fonction "+ methodName +" :</strong></p>");
                    aff.println(result.toString());
                }
            } else {
                // Le type d'objet retourné n'est pas valide, lancer une exception TypeException
                throw new TypeException("Erreur: Le type d'objet retourné n'est pas valide (String ou ModelView attendu)");
            }
        } catch (UrlException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (TypeException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            aff.println(e.getLocalizedMessage());
        }
    }       
}
