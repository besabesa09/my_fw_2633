package p16.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import p16.annotation.Argument;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class TraiteRequest {
    private static Object convertValue(Class<?> type, String value) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.valueOf(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.valueOf(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (type.equals(java.sql.Timestamp.class)) {
            try {
                // Pour un attribut comme "Timestamp dtChange"
                return new java.sql.Timestamp(Long.parseLong(value));
            } catch (NumberFormatException e) {
                try {
                    // Essayer le format standard
                    return java.sql.Timestamp.valueOf(value);
                } catch (Exception e1) {
                    try {
                        // Pour le format "2024-07-20T01:00"
                        if (value.contains("T")) {
                            // Convertir le format ISO-8601 partiel en format compatible avec Timestamp
                            // Format attendu par valueOf: yyyy-MM-dd HH:mm:ss[.f...]
                            String formattedValue = value.replace('T', ' ') + ":00";
                            return java.sql.Timestamp.valueOf(formattedValue);
                        }
                    } catch (Exception e2) {
                        // Dernière tentative avec LocalDateTime
                        try {
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(value);
                            return java.sql.Timestamp.valueOf(ldt);
                        } catch (Exception e3) {
                            throw new IllegalArgumentException("Format de timestamp invalide: " + value +
                                    "\nVeuillez entrer une date valide au format YYYY-MM-DD HH:MM:SS", e3);
                        }
                    }
                }
                return null;
            }
        } else {
            return value;
        }
    }

    public static List<Object> getParameterValuesCombined(Method method, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        List<Object> parameterValues = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();

            if (paramType == MySession.class) {
                HttpSession session = request.getSession();
                MySession mySession = new MySession(session);
                parameterValues.add(mySession);
            } else if (paramType == Part.class) {
                String paramName = parameterNamesArray[i];
                Part filePart = request.getPart(paramName);
                if (filePart == null) {
                    throw new IllegalArgumentException("Fichier manquant pour le paramètre: " + paramName);
                }
                parameterValues.add(filePart);
            } else if (!paramType.isPrimitive() && paramType != String.class) {
                parameterValues.add(objectParameter(param, request, response));
            } else {
                parameterValues.add(primitiveAndString(param, request, parameterNamesArray[i]));
            }
        }

        return parameterValues;
    }

    // private static Object objectParameter(Parameter param, HttpServletRequest request, HttpServletResponse response)
    //         throws Exception {
    //     Valider validation = new Valider();
    //     if (param.isAnnotationPresent(Argument.class)) {
    //         Argument argument = param.getAnnotation(Argument.class);
    //         String prefix = argument.value() + ".";
    //         Class<?> paramType = param.getType();
    //         Object paramObject = paramType.newInstance();
    //         boolean misyErreur = false;

    //         for (Field field : paramType.getDeclaredFields()) {
    //             field.setAccessible(true);
    //             String fieldName = field.getName();
    //             String value = request.getParameter(prefix + fieldName);

    //             if (value == null) {
    //                 String key = "error_" + fieldName;
    //                 request.setAttribute(key, "Paramètre manquant ou invalide: " + fieldName);
    //                 misyErreur = true;
    //                 continue; // Continuer la validation des autres champs
    //             }

    //             String erreur = validation.isFieldValid(field, value);
    //             if (erreur == null) {
    //                 field.set(paramObject, convertValue(field.getType(), value));
    //                 String key = "value_" + fieldName;
    //                 request.setAttribute(key, value);
    //             } else {
    //                 String key = "error_" + fieldName;
    //                 request.setAttribute(key, erreur);
    //                 misyErreur = true;
    //             }
    //         }

    //         if (misyErreur) {
    //             String page = validation.getPreviousPage(request);
    //             request.getRequestDispatcher("/" + page + "?page_principale=" + page).forward(request, response);
    //         }

    //         return paramObject;
    //     } else {
    //         throw new IllegalArgumentException("L'objet doit être annoté avec @Argument");
    //     }
    // }

    private static Object objectParameter(Parameter param, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Valider validation = new Valider();
        if (param.isAnnotationPresent(Argument.class)) {
            Argument argument = param.getAnnotation(Argument.class);
            String prefix = argument.value() + ".";
            Class<?> paramType = param.getType();
            Object paramObject = paramType.newInstance();
            boolean misyErreur = false;

            // Stocker la page actuelle
            String currentPage = validation.getPreviousPage(request);
            
            for (Field field : paramType.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String value = request.getParameter(prefix + fieldName);

                if (value == null) {
                    // Stocker directement l'erreur avec le préfixe "error_"
                    request.setAttribute("error_" + fieldName, "Paramètre manquant ou invalide: " + fieldName);
                    misyErreur = true;
                    continue;
                }

                String erreur = validation.isFieldValid(field, value);
                if (erreur == null) {
                    field.set(paramObject, convertValue(field.getType(), value));
                    // Stocker directement la valeur avec le préfixe "value_"
                    request.setAttribute("value_" + fieldName, value);
                } else {
                    // Stocker directement l'erreur avec le préfixe "error_"
                    request.setAttribute("error_" + fieldName, erreur);
                    // Ajouter également la valeur précédente pour la réafficher
                    request.setAttribute("value_" + fieldName, value);
                    misyErreur = true;
                }
            }

            if (misyErreur) {
                // Ajouter page_principale comme attribut de requête
                request.setAttribute("page_principale", currentPage);
                // Forward à la page précédente avec les erreurs
                request.getRequestDispatcher("/" + currentPage).forward(request, response);
                return null;
            }

            return paramObject;
        } else {
            throw new IllegalArgumentException("L'objet doit être annoté avec @Argument");
        }
    }

    private static Object primitiveAndString(Parameter param, HttpServletRequest request, String paramName)
            throws Exception {
        String value = null;

        if (param.isAnnotationPresent(Argument.class)) {
            Argument argument = param.getAnnotation(Argument.class);
            value = request.getParameter(argument.value());
        } else {
            String[] requestParamNames = request.getParameterMap().keySet().toArray(new String[0]);
            boolean found = false;

            for (String requestParamName : requestParamNames) {
                if (requestParamName.equals(paramName)) {
                    found = true;
                    value = request.getParameter(requestParamName);
                    break;
                }
            }

            if (!found) {
                throw new Exception("ETU2633 : tsisy annotation");
            }
        }

        if (value == null) {
            throw new IllegalArgumentException("Paramètre manquant ou invalide: " + param.getName());
        }
        return value;
    }

    public static Class<?>[] getParameterTypes(Class<?> controllerClass, String methodName) throws NoSuchMethodException {
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getParameterTypes();
            }
        }
        throw new NoSuchMethodException("Méthode non trouvée : " + methodName);
    }
}