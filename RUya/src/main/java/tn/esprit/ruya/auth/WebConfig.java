package tn.esprit.ruya.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Appliquer à toutes les routes
                .allowedOrigins("http://localhost:4200") // Frontend Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Autoriser tous les headers
                .allowCredentials(true); // Si tu utilises des cookies/sessions
    }
}
