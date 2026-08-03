package com.rag.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // 1. Constant Variable (Not a method). Must be Base64 encoded and >= 256 bits.
    private static final String SECRET_KEY = "VGhpcy1Jcy1BLVZlcnktU2VjdXJlLUVudGVycHJpc2UtS2V5LTIwMjY=";

    // 2. Helper Method to get the cryptographic signing key
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 3. Generate Token for the user
    public String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                // Sets expiration to exactly 24 hours from right now
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    // 4. Extract User ID (Subject) from an incoming token
    public String extractUserId(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return payload.getSubject();
    }
}