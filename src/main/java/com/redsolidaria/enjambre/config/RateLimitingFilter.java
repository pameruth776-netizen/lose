package com.redsolidaria.enjambre.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de rate limiting para proteger endpoints de autenticación
 * contra ataques de fuerza bruta.
 * 
 * Límites:
 * - /api/auth/login: 5 intentos por minuto por IP
 * - /login (POST):   5 intentos por minuto por IP
 * - /api/auth/registro: 3 intentos por minuto por IP
 * - Otros:           20 requests por minuto por IP
 */
@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Map<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();
    
    private static final long WINDOW_SIZE_MS = TimeUnit.MINUTES.toMillis(1);
    
    // Límites específicos por patrón de URL
    private static final int LOGIN_LIMIT = 5;
    private static final int REGISTER_LIMIT = 3;
    private static final int DEFAULT_LIMIT = 20;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIp(httpRequest);
        String requestUri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Solo aplicar rate limiting a endpoints específicos
        if (!isRateLimitedEndpoint(requestUri, method)) {
            chain.doFilter(request, response);
            return;
        }
        
        String key = clientIp + ":" + requestUri;
        long now = System.currentTimeMillis();
        
        RateLimitEntry entry = requestCounts.compute(key, (k, v) -> {
            if (v == null || (now - v.windowStart) > WINDOW_SIZE_MS) {
                return new RateLimitEntry(now, 1);
            }
            v.count++;
            return v;
        });
        
        int limit = getLimitForEndpoint(requestUri);
        
        // Agregar cabeceras de rate limiting
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - entry.count)));
        httpResponse.setHeader("X-RateLimit-Reset", String.valueOf((entry.windowStart + WINDOW_SIZE_MS) / 1000));
        
        if (entry.count > limit) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(
                "{\"error\":\"❌ Demasiados intentos. Espera 1 minuto antes de volver a intentarlo.\"}"
            );
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimitedEndpoint(String uri, String method) {
        if (uri == null) return false;
        
        // Login endpoints
        if (uri.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) return true;
        if (uri.equals("/login") && "POST".equalsIgnoreCase(method)) return true;
        
        // Registro endpoints
        if (uri.startsWith("/api/auth/registro") && "POST".equalsIgnoreCase(method)) return true;
        if (uri.startsWith("/registro/") && "POST".equalsIgnoreCase(method)) return true;
        
        // Verificación de código
        if (uri.equals("/api/auth/verificar-codigo") && "POST".equalsIgnoreCase(method)) return true;
        if (uri.equals("/verificar-codigo") && "POST".equalsIgnoreCase(method)) return true;
        
        // Check email (evitar enumeración rápida)
        if (uri.equals("/api/auth/check-email") && "GET".equalsIgnoreCase(method)) return true;
        
        return false;
    }
    
    private int getLimitForEndpoint(String uri) {
        if (uri == null) return DEFAULT_LIMIT;
        
        if (uri.contains("/login")) return LOGIN_LIMIT;
        if (uri.contains("/registro")) return REGISTER_LIMIT;
        if (uri.contains("/verificar-codigo")) return LOGIN_LIMIT;
        
        return DEFAULT_LIMIT;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    
    private static class RateLimitEntry {
        final long windowStart;
        int count;
        
        RateLimitEntry(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}