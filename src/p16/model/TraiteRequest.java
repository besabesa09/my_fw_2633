package p16.model;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import p16.annotation.Argument;

public class TraiteRequest {
    public static List<Object> parameterMethod(Method method, HttpServletRequest request) {
        List<Object> parameterValues = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            String argument_value = null;
                if (parameter.isAnnotationPresent(Argument.class)) {
                    Argument argument = parameter.getAnnotation(Argument.class);
                    argument_value = argument.value();
                    String value = request.getParameter(argument_value);
                if (value != null) {
                    parameterValues.add(value);
                }else{
                    throw new IllegalArgumentException("Paramètre manquant: " + argument_value);
                }
            }
        }    
        return parameterValues;
    }
}
