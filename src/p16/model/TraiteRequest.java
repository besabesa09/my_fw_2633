package p16.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import p16.annotation.Argument;

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
        } else {
            return value;
        }
    }   

    public static List<Object> getParameterValuesCombined(Method method, HttpServletRequest request) throws Exception {
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
            
            }else if (paramType == Part.class) {
            // Gérer les fichiers via Part
            String paramName = parameterNamesArray[i];
            Part filePart = request.getPart(paramName);
            if (filePart == null) {
                throw new IllegalArgumentException("Fichier manquant pour le paramètre: " + paramName);
            }
            parameterValues.add(filePart);

            } else if (!paramType.isPrimitive() && paramType != String.class) {
                // Appel de la méthode objectParameter pour gerer les parametres etant un Objet
                parameterValues.add(objectParameter(param, request));
            } else {
                // Appel de la méthode les types primitifs ou String
                parameterValues.add(primitiveAndString(param, request, parameterNamesArray[i]));
            }
        }
        return parameterValues;
    }

    // si le parametre est un objet
    public static Object objectParameter(Parameter param, HttpServletRequest request) throws Exception {
        Valider validation = new Valider();
        if (param.isAnnotationPresent(Argument.class)) {
            Argument argument = param.getAnnotation(Argument.class);
            String prefix = argument.value() + ".";
    
            Class<?> paramType = param.getType();
            Object paramObject = paramType.newInstance();
    
            for (Field field : paramType.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String value = request.getParameter(prefix + fieldName);
    
                if (value == null) {
                    throw new IllegalArgumentException("Paramètre manquant ou invalide: " + fieldName);
                }
    
                // Effectue une validation de la valeur avant de la définir
                if (validation.isValidate(field, value)) {
                    field.set(paramObject, convertValue(field.getType(), value));
                } else {
                    throw new IllegalArgumentException("Validation échouée pour le champ: " + fieldName);
                }
            }
            return paramObject;
        } else {
            throw new IllegalArgumentException("L'objet doit être annoté avec @Argument");
        }
    }

    private static Object primitiveAndString(Parameter param, HttpServletRequest request, String paramName) throws Exception {
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
}
