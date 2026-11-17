package cl.duoc.lumina.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SeguridadConfig {

    // Todas las reglas de acceso por rol están aquí. Se revisan en orden: gana la primera que calce.
    @Bean
    SecurityFilterChain filtros(HttpSecurity http, FiltroJwt filtroJwt) throws Exception {
        http
                // Sin cookies de sesión (el token va en un header), CSRF no aplica.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin token válido se responde 401, que el frontend usa para renovar el token.
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/registro", "/api/auth/refresh").permitAll()
                        // Catálogo: cualquiera lo ve; vendedor y admin lo editan; solo admin borra.
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/productos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers("/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        // Órdenes: cualquier usuario con sesión compra y ve las suyas; el resto es de gestión.
                        .requestMatchers(HttpMethod.POST, "/api/ordenes").authenticated()
                        .requestMatchers("/api/ordenes/mias").authenticated()
                        .requestMatchers("/api/ordenes/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .anyRequest().authenticated())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // BCrypt guarda un hash con sal: aunque alguien lea la base, no ve las contraseñas.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
