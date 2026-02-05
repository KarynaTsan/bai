package com.karina.bai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    // 10 prób za n sec na IP
    private static final int LIMIT = 10;
    private static final long WINDOW_SEC = 50;

    private final Map<String, Window> storage = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean sensitive =
                (path.equals("/login") && request.getMethod().equalsIgnoreCase("POST")) ||
                        (path.equals("/register") && request.getMethod().equalsIgnoreCase("POST"));

        if (!sensitive) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        System.out.println("RATE LIMIT CHECK for IP=" + ip);
        long now = Instant.now().getEpochSecond();

        Window w = storage.compute(ip, (k, old) -> {
            if (old == null || now - old.start >= WINDOW_SEC) {
                return new Window(now, 1);
            }
            old.count++;
            return old;
        });

        if (w.count > LIMIT) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private static class Window {
        long start;
        int count;
        Window(long start, int count) { this.start = start; this.count = count; }
    }
}
