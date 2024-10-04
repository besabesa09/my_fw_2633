package p16.controller;

import p16.annotation.Controller;
import p16.annotation.Get;
import p16.annotation.Post;
import p16.annotation.RestApi;
import p16.annotation.Url;
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

import com.google.gson.Gson;

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
                    if (method.isAnnotationPresent(Url.class)) {
                       //nom_classe et nom_methode
                        String className = controller.getName();
                        String methodName = method.getName();
                        String Verb = "GET"; // Par défaut, GET

                        //@Url value
                        Url urlAnnotation = method.getAnnotation(Url.class);
                        String url = urlAnnotation.value();

                        if (method.isAnnotationPresent(Post.class)) {
                            Verb = "POST";
                        } else if (method.isAnnotationPresent(Get.class)) {
                            Verb = "GET";
                        }

                        if (urlMappings.containsKey(url)) {
                        // L'URL est en double, lancer une exception DuplicateUrlException
                            throw new DuplicateUrlException("Erreur: L'URL " + url + " est déjà utilisée");
                        }

                        Mapping mapping = new Mapping(className, methodName,Verb);
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
        String verb = mapping.getVerb();

        // Vérifier si le verbe de req correspond au verbe attendu (get ou post)
        String requestMethod = req.getMethod();
        if (!requestMethod.equalsIgnoreCase(verb)) {
           throw new IllegalArgumentException("Erreur: La méthode HTTP '" + requestMethod + "' ne correspond pas avec l'annotation @" + verb + " pour l'URL " + url);
        }
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
        
        boolean isRestApi = method.isAnnotationPresent(RestApi.class);
         if (isRestApi) {
            // La méthode est annotée avec @RestApi, on traite le résultat en JSON
            resp.setContentType("application/json");

            if (result instanceof ModelView) {
                // Transformer le 'data' du ModelView en JSON
                ModelView modelView = (ModelView) result;
                HashMap<String, Object> data = modelView.getData();
                String json = new Gson().toJson(data);
                aff.print(json);
            } else {
                // Transformer directement le résultat en JSON
                String json = new Gson().toJson(result);
                aff.print(json);
            }
        } else {  
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
