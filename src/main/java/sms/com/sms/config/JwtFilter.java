package sms.com.sms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import sms.com.sms.service.UserService;
import sms.com.sms.service.UserServiceImpl;

@Component
//@RequiredArgsConstructor  // ✅ This ensures dependencies are injected automatically
public class JwtFilter extends OncePerRequestFilter {
 private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtFilter(@Lazy UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    
        final String authorizationHeader = request.getHeader("Authorization");
    
        System.out.println("🔹 Authorization Header: " + authorizationHeader); // Debugging
    
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("❌ No JWT token found. Skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }
    
        String jwt = authorizationHeader.substring(7);
        String phoneNumber = jwtUtil.extractUsername(jwt);
    
        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
    
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ JWT Token Authenticated for: " + phoneNumber);
            } else {
                System.out.println("❌ JWT Token is invalid.");
            }
        }
    
        filterChain.doFilter(request, response);
    }
    
}
