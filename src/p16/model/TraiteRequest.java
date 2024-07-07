package p16.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
// import java.util.Arrays;
// import java.util.Map;
// import java.util.stream.Collectors;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import p16.annotation.Argument;

public class TraiteRequest {
    public static List<Object> getParameterValues(Method method, HttpServletRequest request) throws Exception {
        List<Object> parameterValues = new ArrayList<>();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);
    
        // Récupérer les noms des paramètres de la méthode en utilisant la réflexion
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String value = null;
            if (param.isAnnotationPresent(Argument.class)) {
                Argument argument = param.getAnnotation(Argument.class);
                String arg_name = argument.value();
                value = request.getParameter(arg_name);
            } else {
                String paramName = parameterNamesArray[i];
                // Récupérer les "name" venant du formulaire
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
                    throw new IllegalArgumentException("Le paramètre " + paramName + " n'existe pas dans la méthode");
                }
            }
            if (value == null) {
                throw new IllegalArgumentException("Paramètre manquant ou invalide: " + param.getName());
            }
            parameterValues.add(value);
        }
        return parameterValues;
    }
    
    // public static List<Object> getParameterValuesForObjects(Method method, HttpServletRequest request) throws Exception {
    // List<Object> parameterValues = new ArrayList<>();
    // Paranamer paranamer = new AdaptiveParanamer();
    // String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);

    // Parameter[] parameters = method.getParameters();
    // for (int i = 0; i < parameters.length; i++) {
    //     int index = i;
    //     Parameter param = parameters[i];
    //     Class<?> paramType = param.getType();

    //     if (!paramType.isPrimitive() && paramType != String.class) {
    //         Object paramObject = paramType.newInstance();

    //         Map<String, String[]> parameterMap = request.getParameterMap().entrySet().stream()
    //                 .filter(entry -> entry.getKey().startsWith(parameterNamesArray[index] + "."))
    //                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    //         for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
    //             String key = entry.getKey();
    //             String attributeName = key.substring(key.lastIndexOf(".") + 1);
    //             String value = entry.getValue()[0];

    //             try {
    //                 Field field = paramType.getDeclaredField(attributeName);
    //                 field.setAccessible(true);
    //                 field.set(paramObject, convertValue(field.getType(), value));
    //             } catch (Exception e) {
    //                 throw new IllegalArgumentException("Impossible de définir la valeur du paramètre " + key, e);
    //             }
    //         }

    //         parameterValues.add(paramObject);
    //     } else {
    //         throw new IllegalArgumentException("Le paramètre " + param.getName() + " n'est pas un objet.");
    //     }
    // }
    // return parameterValues;
    // }

    private static Object convertValue(Class<?> type, String value) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.valueOf(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.valueOf(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else {
            return value;
        }
    }   
    
    // public static List<Object> getParameterValuesForMethod(Method method, HttpServletRequest request) throws Exception {
    //     List<Object> parameterValues = new ArrayList<>();

    //     Parameter[] parameters = method.getParameters();
    //     boolean hasObjectParameter = Arrays.stream(parameters)
    //             .anyMatch(param -> !param.getType().isPrimitive() && param.getType() != String.class);
    //     if (hasObjectParameter) {
    //         parameterValues = getParameterValuesForObjects(method, request);
    //     } else {
    //         parameterValues = getParameterValues(method, request);
    //     }
    //     return parameterValues;
    // }

    public static List<Object> getParameterValuesCombined(Method method, HttpServletRequest request) throws Exception {
        List<Object> parameterValues = new ArrayList<>();
        Paranamer paranamer = new AdaptiveParanamer();
        String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);
    
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            
            if (paramType == MySession.class) {
            HttpSession session = request.getSession();
            MySession mySession = new MySession(session);
            parameterValues.add(mySession);
    
            } else if (!paramType.isPrimitive() && paramType != String.class) {
                if (param.isAnnotationPresent(Argument.class)) {
                    Argument argument = param.getAnnotation(Argument.class);
                    String prefix = argument.value() + ".";
    
                    Object paramObject = paramType.newInstance();
                    for (Field field : paramType.getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        String value = request.getParameter(prefix + fieldName);
    
                        if (value == null) {
                            throw new IllegalArgumentException("Paramètre manquant ou invalide: " + fieldName);
                        }
    
                        field.set(paramObject, convertValue(field.getType(), value));
                    }
    
                    parameterValues.add(paramObject);
                } else {
                    throw new IllegalArgumentException("L'objet doit être annoté avec @Argument");
                }
                
            } else {
                String value = null;
                if (param.isAnnotationPresent(Argument.class)) {
                    Argument argument = param.getAnnotation(Argument.class);
                    value = request.getParameter(argument.value());
                } else {
                    String paramName = parameterNamesArray[i];
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
                parameterValues.add(value);
            }
        }
        return parameterValues;
    }
}
