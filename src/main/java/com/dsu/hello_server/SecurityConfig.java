package com.dsu.hello_server;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class SecurityConfig {

    // Rules are read from the top: the first match wins.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            ApiAuthenticationEntryPoint entryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(POST, "/api/auth/signup", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers(GET, "/api/auth/me").authenticated()
                        .requestMatchers(GET, "/api/reservations/**").authenticated()
                        .requestMatchers(GET, "/api/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(o -> o
                        .authenticationEntryPoint(entryPoint)
                        .jwt(Customizer.withDefaults()))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HS256: the same secret signs and checks. It needs at least 32 bytes.
    @Bean
    public SecretKey jwtKey(@Value("${app.jwt.secret}") String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey) {
        return NimbusJwtDecoder.withSecretKey(jwtKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
