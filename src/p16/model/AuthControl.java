package p16.model;

import java.lang.annotation.Annotation;
import jakarta.servlet.http.HttpServletRequest;
import p16.annotation.auth.Auth;

public class AuthControl {

    // Partie issue de AuthManager
    public boolean isHabilitate(Annotation annotation, HttpServletRequest request) {
        if (annotation != null) {
            int auth_level = ((Auth) annotation).level();
            int client_level = 0;
            if (request.getSession().getAttribute("auth_level") != null) {
                client_level = (int) request.getSession().getAttribute("auth_level");
            }

            if (auth_level > client_level) {
                return false;
            }
        }
        return true;
    }

    // Partie issue de Auth
    public void setAuthLevel(HttpServletRequest request, int level) {
        request.getSession().setAttribute("auth_level", level);
    }
}
