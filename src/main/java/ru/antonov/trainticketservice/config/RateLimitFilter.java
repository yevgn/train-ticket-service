package ru.antonov.trainticketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.antonov.trainticketservice.common.exception.ApiError;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry registry;
    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isPurchaseRequest(request)) {
            String ip = extractIp(request);
            RateLimiter limiter = registry.rateLimiter("purchase-" + ip, "default");

            boolean allowed = limiter.acquirePermission();

            if (!allowed) {
                ApiError error = ApiError.builder()
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .message("Слишком много запросов. Попробуйте позже")
                        .build();

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String json = mapper.writeValueAsString(error);
                response.getWriter().write(json);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPurchaseRequest(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/tickets/purchase")
                && request.getMethod().equalsIgnoreCase("POST");
    }

    // подсмотрел у LLM
    private String extractIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");

        if (header != null && !header.isBlank()) {
            return header.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}
