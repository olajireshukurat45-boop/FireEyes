package sms.com.sms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import sms.com.sms.service.UserService;

import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(@Lazy UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ❌ Disable CSRF for JWT authentication
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CORS Enabled
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅
                                                                                                              // Stateless
                                                                                                              // session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/sendOtp/**",
                                "/user/verifyOtpAndCreateUser/**",
                                "/user/auth/login",
                                "/user/reset-password/**",
                                "/user/sendOtpToEmail/**", // <-- fixed casing
                                "/user/forgotPassword/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/gas-detectors/admin/update/**",
                                "/gas-detectors/users/**")
                        .permitAll()
                        .requestMatchers("/user/admin/**").hasAuthority("ROLE_ADMIN")
                        // ✅ Allow /gas-detectors/user/assign for both USER and ADMIN
                      .requestMatchers("/gas-detectors/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        .requestMatchers("/gas-detectors/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // ✅ Use hasAuthority
                        .requestMatchers("/user/**","/user/user-gas-details/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") // ✅ Use hasAnyAuthority
                        .anyRequest().authenticated() // ✅ Other endpoints require authentication
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // ✅ Ensure JwtFilter is added

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ✅ Use BCrypt for password encoding
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // ✅ Allow all origins (Adjust for production)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization")); // ✅ Ensure JWT is exposed

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
