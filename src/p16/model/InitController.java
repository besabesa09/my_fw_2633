package p16.model;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import p16.annotation.Get;
import p16.annotation.Post;
import p16.annotation.Url;
import p16.exception.DuplicateUrlException;
import p16.exception.NoPackageException;

public class InitController {

    private HashMap<String, Mapping> urlMappings = new HashMap<>();

    public HashMap<String, Mapping> initialize(String packageName)
            throws ClassNotFoundException, IOException, DuplicateUrlException, NoPackageException {
        if (packageName == null || packageName.isEmpty()) {
            throw new NoPackageException("Erreur: Le package_name est requis et ne peut pas être vide");
        }

        // Récupérer la liste des contrôleurs
        ArrayList<Class<?>> controllers = ScanController.allClasses(packageName);

        // Traiter chaque contrôleur
        for (Class<?> controller : controllers) {
            processControllerMethods(controller);
        }
        return urlMappings;
    }

    private void processControllerMethods(Class<?> controller) throws DuplicateUrlException {
        for (Method method : controller.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Url.class)) {
                processAnnotatedMethod(controller, method);
            }
        }
    }

    private void processAnnotatedMethod(Class<?> controller, Method method) throws DuplicateUrlException {
        String className = controller.getName();
        String methodName = method.getName();
        String httpVerb = getHttpVerb(method);
        String url = getUrlFromAnnotation(method);

        Mapping map = urlMappings.get(url);
        if (map == null) {
            createNewMapping(url, className, methodName, httpVerb);
        } else {
            addVerbActionToMapping(map, methodName, httpVerb, url);
        }
    }

    private String getHttpVerb(Method method) {
        if (method.isAnnotationPresent(Post.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(Get.class)) {
            return "GET";
        }
        return "GET"; // Par défaut, GET
    }

    private String getUrlFromAnnotation(Method method) {
        Url urlAnnotation = method.getAnnotation(Url.class);
        return urlAnnotation.value();
    }

    private void createNewMapping(String url, String className, String methodName, String httpVerb) {
        Mapping mapping = new Mapping(className);
        mapping.addVerbAction(new VerbAction(methodName, httpVerb));
        urlMappings.put(url, mapping);
    }

    private void addVerbActionToMapping(Mapping map, String methodName, String httpVerb, String url)
            throws DuplicateUrlException {
        if (!isVerbExists(map, httpVerb)) {
            map.addVerbAction(new VerbAction(methodName, httpVerb));
        } else {
            throw new DuplicateUrlException(
                    "Erreur: L'URL " + url + " avec le verbe " + httpVerb + " est déjà utilisée");
        }
    }

    private boolean isVerbExists(Mapping map, String httpVerb) {
        for (VerbAction existingVerbAction : map.getVerbAction()) {
            if (existingVerbAction.getVerb().equalsIgnoreCase(httpVerb)) {
                return true;
            }
        }
        return false;
    }
}
