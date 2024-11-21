package dev.portero.oauth;

import dev.portero.oauth.security.dto.RegistrationRequest;
import dev.portero.oauth.security.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuthApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(AuthService authService) {
        return (args) -> {
            RegistrationRequest request = RegistrationRequest.builder()
                    .email("admin@admin.com")
                    .username("admin")
                    .password("password")
                    .build();
            authService.register(request);
        };
    }
}