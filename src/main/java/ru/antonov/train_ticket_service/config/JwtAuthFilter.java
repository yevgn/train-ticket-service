package ru.antonov.train_ticket_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.antonov.train_ticket_service.auth.entity.TokenMode;
import ru.antonov.train_ticket_service.auth.service.JwtService;
import ru.antonov.train_ticket_service.common.exception.ApiError;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    @Value("${server.servlet.context-path}")
    private String servletContextPath;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        if(SecurityContextHolder.getContext().getAuthentication() == null){
            String username = "";
            if(jwtService.isTokenValid(jwt, TokenMode.ACCESS)){
                try{
                    username = jwtService.extractUsername(jwt);
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } catch (JwtException | IllegalArgumentException ex){
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                    ApiError error = ApiError.builder()
//                            .status(HttpStatus.UNAUTHORIZED)
//                            .message("Неуспешная валидация токена")
//                            .build();
//                    objectMapper.writeValue(response.getOutputStream(), error);
//                    return;
                } catch (UsernameNotFoundException ex) {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                    ApiError error = ApiError.builder()
//                            .status(HttpStatus.UNAUTHORIZED)
//                            .message("Неуспешная валидация токена")
//                            .build();
//                    objectMapper.writeValue(response.getOutputStream(), error);
//                    log.warn("Ошибка аутентификации. Пользователя, которому выдан токен, не существует: {}", username);
//                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//
//        return HttpMethod.OPTIONS.matches(request.getMethod())
//                || uri.contains(servletContextPath + "/auth/make-auth")
//                || uri.contains(servletContextPath + "/auth/refresh-access-token")
//                || uri.contains(servletContextPath + "/auth/send-mail-for-password-reset")
//                || uri.contains(servletContextPath + "/auth/reset-password")
//                || uri.contains(servletContextPath + "/auth/activate-account")
//                || uri.contains(servletContextPath + "/swagger-ui/")
//                || uri.contains(servletContextPath + "/v3/api-docs");
//    }
}
