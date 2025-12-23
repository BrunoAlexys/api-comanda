package br.com.apicomanda.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Importação estática da sua constante
import static br.com.apicomanda.helpers.ApplicationConstants.VERSION;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Configuração do CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Libera OPTIONS globalmente (Necessário para CORS funcionar com Security)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Libera o PATCH para atualização de status
                        // O Spring concatena: "/v1" + "/api/orders/**" = "/v1/api/orders/**"
                        .requestMatchers(HttpMethod.PATCH, VERSION + "/api/orders/**").permitAll()

                        // 3. Outras rotas públicas (Login, Cadastro, WebSocket, Cozinha)
                        .requestMatchers(HttpMethod.POST, VERSION + "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, VERSION + "/api/users").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(VERSION + "/api/orders/kitchen/**").permitAll()
                        .requestMatchers(VERSION + "/api/orders/kitchen/statistics/average-time/**").permitAll()

                        // 4. Todo o resto requer token
                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origem do Front
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Métodos (PATCH e OPTIONS são cruciais)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));

        // Credenciais
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}