package com.rag.web.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // An in-memory cache to track the buckets assigned to different IP addresses
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Allows 5 requests, refilling exactly 5 tokens every 1 minute
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Identify the user by their IP address
        String ipAddress = request.getRemoteAddr();

        // Fetch their existing bucket, or create a new one if it's their first time visiting
        Bucket bucket = cache.computeIfAbsent(ipAddress, k -> createNewBucket());

        // Try to consume 1 token. If successful, let them through.
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // If they are out of tokens, block the request and return a 429 status code
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please wait a minute before trying again.");
        }
    }
}