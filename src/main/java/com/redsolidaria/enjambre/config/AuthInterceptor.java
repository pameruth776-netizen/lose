package com.redsolidaria.enjambre.config;

import com.redsolidaria.enjambre.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Usuario usuario = session != null ? (Usuario) session.getAttribute("usuario") : null;
        
        String path = request.getRequestURI();
        
        // Determinar si es una solicitud API
        boolean isApi = path.startsWith("/api/");
        
        // Rutas públicas que no requieren autenticación
        if (path.startsWith("/api/auth") || path.equals("/login") || path.equals("/logout") || path.startsWith("/registro")) {
            return true;
        }
        
        if (usuario == null) {
            if (isApi) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"❌ Sesión no válida o expirada\"}");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }
        
        String rol = usuario.getRol();
        
        // Reglas de autorización
        boolean isAuthorized = true;
        
        if ((path.startsWith("/admin") || path.startsWith("/api/admin")) && !"ADMIN".equals(rol)) {
            isAuthorized = false;
        } else if ((path.startsWith("/voluntario") || path.startsWith("/api/voluntario")) && !"VOLUNTARIO".equals(rol)) {
            isAuthorized = false;
        } else if ((path.startsWith("/discapacitado") || path.startsWith("/api/discapacitado")) && !"DISCAPACITADO".equals(rol)) {
            isAuthorized = false;
        }
        
        if (!isAuthorized) {
            if (isApi) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"❌ No tienes permisos para realizar esta acción\"}");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }
        
        return true;
    }
}
