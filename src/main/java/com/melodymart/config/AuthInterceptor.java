package com.melodymart.config;

import com.melodymart.model.Role;
import com.melodymart.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check if Admin access is required
        boolean adminRequired = handlerMethod.hasMethodAnnotation(AdminRequired.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(AdminRequired.class);

        // Check if general Login is required
        boolean loginRequired = handlerMethod.hasMethodAnnotation(LoginRequired.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(LoginRequired.class) ||
                adminRequired; // Admin access implicitly requires login

        if (loginRequired) {
            HttpSession session = request.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if (currentUser == null) {
                // Not logged in: Redirect to login with original request URI for redirect back
                String requestURI = request.getRequestURI();
                String queryString = request.getQueryString();
                String redirectUrl = requestURI;
                if (queryString != null) {
                    redirectUrl += "?" + queryString;
                }
                
                response.sendRedirect(request.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(redirectUrl, "UTF-8"));
                return false;
            }

            if (adminRequired && currentUser.getRole() != Role.ADMIN) {
                // Logged in but not an admin: Access Denied
                response.sendRedirect(request.getContextPath() + "/error?message=" + java.net.URLEncoder.encode("Access denied. Administrator privileges required.", "UTF-8"));
                return false;
            }
        }

        return true;
    }
}
