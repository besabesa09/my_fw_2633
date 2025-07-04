package p16.model;

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import p16.annotation.validation.*;

public class Valider {
    // public boolean isValidate(Field attr, String value) throws Exception {
    //     boolean result = true;

    //     if (attr.isAnnotationPresent(Numerique.class)) {
    //         if (!value.matches("-?\\d+(\\.\\d+)?")) {
    //             result = false;
    //             throw new Exception("L'input de l'attribut " + attr.getName() + " doit être de type numérique");
    //         }
    //     }

    //     if (attr.isAnnotationPresent(Email.class)) {
    //         if (!value.contains("@")) {
    //             result = false;
    //             throw new Exception("L'input de l'attribut " + attr.getName() + " doit contenir un @, comme un mail");
    //         }
    //     }

    //     if (attr.isAnnotationPresent(Required.class)) {
    //         if (value == null || value.isEmpty()) {
    //             result = false;
    //             throw new Exception("L'input de l'attribut " + attr.getName() + " doit être complété");
    //         }
    //     }

    //     return result;
    // }

    public String isFieldValid (Field field, String value) throws Exception {
        String error = null;
        if (field.isAnnotationPresent(Numerique.class)) {
            if (!value.matches("-?\\d+(\\.\\d+)?")){
                error = "La valeur du champ doit etre numerique";
            }
        }

        if (field.isAnnotationPresent(Email.class)){
            if (!value.contains("@")){
                error = "La valeur du champ doit etre de type Mail";
            }
        }

        if (field.isAnnotationPresent(Required.class)){
            if (value == null || value.equals("")) {
                error = "Le champ ne peut pas etre vide";
            }
        }

        return error;
    }

    public String getPreviousPage(HttpServletRequest request) {
        HttpSession sess = request.getSession();
        String page = (String)sess.getAttribute("page_principale");
        if (page == null) {
            page = request.getHeader("Referer").substring(request.getHeader("Referer").lastIndexOf("/") + 1);
            sess.setAttribute("page_principale", page);
        }
        return page;
    }

    public String setPreviousPage(String error, HttpServletRequest request){
        String page = request.getParameter("page_principale");
        String result = "";
        if (page != null) {
            result = "<input type='hidden' name='page_principale' value='" + page + "' >";
        } 
        return error + " " + result;
    }

    // public static String error (String field_name, HttpServletRequest request){
    //     String error = (String) request.getAttribute("error_"+field_name);
    //     if (error == null) { 
    //         error = "";
    //     }
    //     return new Valider().setPreviousPage(error, request);
    // }

    // public static String value (String field_name, HttpServletRequest request){
    //     String value = (String) request.getAttribute("value_"+field_name);
    //     if (value == null) {
    //         value = "";
    //     }
    //     return value;
    // }
}