package com.redsolidaria.enjambre.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/admin/**",
                        "/voluntario/**",
                        "/discapacitado/**",
                        "/api/admin/**",
                        "/api/voluntario/**",
                        "/api/discapacitado/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve archivos subidos desde src/main/resources/static/uploads/ y target/classes/static/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:src/main/resources/static/uploads/",
                        "classpath:/static/uploads/"
                );
    }
}
