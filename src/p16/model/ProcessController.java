package p16.model;

import p16.annotation.RestApi;
import p16.annotation.auth.Auth;
import p16.exception.TypeException;
import p16.exception.AuthException;
import p16.exception.ErrorPage;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProcessController {

    private Map<String, Mapping> urlMappings;

    public ProcessController(Map<String, Mapping> urlMappings) {
        this.urlMappings = urlMappings;
    }

    public Mapping getMappingForUrl(String url) {
        return urlMappings.get(url);
    }

    public VerbAction getVerbActionForRequest(Mapping mapping, String verb) {
        for (VerbAction va : mapping.getVerbAction()) {
            if (va.getVerb().compareToIgnoreCase(verb) == 0) {
                return va;
            }
        }
        return null;
    }

    // public Object invokeControllerMethod(Mapping mapping, VerbAction verbAction, HttpServletRequest req, HttpServletResponse resp)
    //         throws Exception {
    //     String controllerName = mapping.getClassName();
    //     String methodName = verbAction.getMethod();

    //     Class<?> controllerClass = Class.forName(controllerName);
    //     Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
    //     Method method = getMethodFromController(controllerClass, methodName);

    //     if (method.getParameterCount() > 0) {
    //         List<Object> parameterValues = TraiteRequest.getParameterValuesCombined(method, req, resp);
    //         if (parameterValues.size() != method.getParameterCount()) {
    //             throw new IllegalArgumentException(
    //                     "nombre de paramètres envoyés différents des paramètres de la méthode");
    //         }
    //         return method.invoke(controllerInstance, parameterValues.toArray());
    //     } else {
    //         return method.invoke(controllerInstance);
    //     }
    // }
    public Object invokeControllerMethod(Mapping mapping, VerbAction verbAction, HttpServletRequest req,
            HttpServletResponse resp)
            throws Exception {
        String controllerName = mapping.getClassName();
        String methodName = verbAction.getMethod();

        Class<?> controllerClass = Class.forName(controllerName);
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
        Method method = getMethodFromController(controllerClass, methodName);

        // Vérification de l'authentification au niveau de la méthode
        Annotation methodAnnotation = method.getAnnotation(Auth.class);
        if (methodAnnotation != null) {
            int requiredLevel = ((Auth) methodAnnotation).level();
            int userLevel = 0;
            if (req.getSession().getAttribute("auth_level") != null) {
                userLevel = (int) req.getSession().getAttribute("auth_level");
            }

            if (requiredLevel > userLevel) {
                throw new AuthException(
                        "Acces refusé: niveau d'authentification invalide pour " + methodName);
            }
        }

        if (method.getParameterCount() > 0) {
            List<Object> parameterValues = TraiteRequest.getParameterValuesCombined(method, req, resp);
            if (parameterValues.size() != method.getParameterCount()) {
                throw new IllegalArgumentException(
                        "nombre de paramètres envoyés différents des paramètres de la méthode");
            }
            return method.invoke(controllerInstance, parameterValues.toArray());
        } else {
            return method.invoke(controllerInstance);
        }
    }

    private Method getMethodFromController(Class<?> controllerClass, String methodName) throws NoSuchMethodException {
        for (Method m : controllerClass.getMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new NoSuchMethodException(controllerClass.getName() + "." + methodName + "()");
    }

    public void handleResponse(Object result, HttpServletRequest req, HttpServletResponse resp, PrintWriter aff,
            Method controllerMethod)
            throws IOException, ServletException {
        try {
            if (result instanceof String || result instanceof ModelView) {
                if (isRestApiResponse(controllerMethod)) {
                    handleApiResponse(result, resp, aff);
                } else if (result instanceof ModelView) {
                    handleModelViewResponse((ModelView) result, req, resp);
                } else {
                    handleStringResponse(result.toString(), req, resp, aff);
                }
            } else if (isRestApiResponse(controllerMethod)) {
                handleApiResponse(result, resp, aff);
            } else {
                throw new TypeException(
                        "Erreur: Le type d'objet retourné n'est pas valide (String, ModelView ou API response attendu)");
            }
        } catch (ServletException | IOException | TypeException e) {
            e.printStackTrace();
        }
    }

    private boolean isRestApiResponse(Method controllerMethod) {
        return controllerMethod != null && controllerMethod.isAnnotationPresent(RestApi.class);
    }

    private void handleApiResponse(Object result, HttpServletResponse resp, PrintWriter aff) throws IOException {
        resp.setContentType("application/json");

        if (result instanceof ModelView) {
            // Extraire les données du ModelView et les convertir en JSON
            ModelView modelView = (ModelView) result;
            HashMap<String, Object> data = modelView.getData();
            String json = new Gson().toJson(data);
            aff.print(json);
        } else {
            String json = new Gson().toJson(result);
            aff.print(json);
        }
    }

    private void handleModelViewResponse(ModelView modelView, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String viewUrl = modelView.getUrl();
        HashMap<String, Object> data = modelView.getData();

        for (String key : data.keySet()) {
            req.setAttribute(key, data.get(key));
        }

        RequestDispatcher dispat = req.getRequestDispatcher(viewUrl);
        dispat.forward(req, resp);
    }

    private void handleStringResponse(String result, HttpServletRequest req, HttpServletResponse resp,
            PrintWriter aff) {
        resp.setContentType("text/html");
        aff.println("<h2>Test sprint 3 </h2>");
        aff.println("<p><strong>Contrôleur</strong> : " + req.getAttribute("controllerName") + "</p>");
        aff.println("<p><strong>Méthode</strong> : " + req.getAttribute("methodName") + "</p>");
        aff.println("<p><strong>Execution de la fonction " + req.getAttribute("methodName") + " :</strong></p>");
        aff.println(result);
    }

    public void sendErrorResponse(HttpServletResponse resp, PrintWriter aff, String errorMessage) {
        resp.setContentType("text/html");
        aff.println(ErrorPage.doError(errorMessage));
    }
}