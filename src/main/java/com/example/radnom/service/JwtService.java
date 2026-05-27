package com.example.radnom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshExpiration;

    @PostConstruct
    public void init() {
        System.out.println("\n ===== JWT SERVICE INIT =====");
        System.out.println("Secret loaded: " +
                (secretKeyString != null ? "YES (" + secretKeyString.length() + " chars)" : "NO"));

        if (secretKeyString != null) {
            System.out.println("First 20 chars: " + secretKeyString.substring(0, Math.min(20, secretKeyString.length())));
        }

        try {
            SecretKey key = getSigningKey();
            System.out.println("Signing key created");
            System.out.println("   Algorithm: " + key.getAlgorithm());

            String testToken = generateToken("test@example.com");
            System.out.println("Test token generated: " + testToken.substring(0, 30) + "...");

            boolean valid = isTokenValid(testToken);
            System.out.println("Test token valid: " + valid);

        } catch (Exception e) {
            System.err.println("JWT init failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=============================\n");
    }

    private SecretKey getSigningKey() {
        System.out.println("🛠 Creating signing key from secret...");

        if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
            throw new RuntimeException("JWT secret is null or empty!");
        }

        try {

            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
            System.out.println("Secret decoded from Base64");
            System.out.println("   Decoded bytes length: " + decodedKey.length);

            return Keys.hmacShaKeyFor(decodedKey);

        } catch (IllegalArgumentException e) {
            System.out.println(" Not Base64, trying as hex...");

            try {
                byte[] hexBytes = hexStringToByteArray(secretKeyString);
                System.out.println(" Secret decoded from hex");
                System.out.println("   Hex bytes length: " + hexBytes.length);

                return Keys.hmacShaKeyFor(hexBytes);

            } catch (Exception e2) {
                System.err.println(" Warning: Using plain text as key (not secure for production!)");

                if (secretKeyString.length() < 32) {
                    System.err.println(" Secret too short for plain text! Min 32 chars");
                    throw new RuntimeException("JWT secret too short");
                }

                return Keys.hmacShaKeyFor(secretKeyString.getBytes());
            }
        }
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public String generateJwtToken(String emailOrUsername) {
        return generateToken(emailOrUsername);
    }

    public Long getExpiration() {
        return expiration;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");

        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Generated refresh token for " + userDetails.getUsername());
        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return "access";
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.err.println("Error extracting username: " + e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.err.println("JWT parsing error: " + e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatePasswordResetToken(String username) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "password_reset");

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public void printTokenInfo(String token) {
        try {
            System.out.println("JWT Token Info:");
            System.out.println("  Username: " + extractUsername(token));
            System.out.println("  Expiration: " + extractExpiration(token));
            System.out.println("  Is expired: " + isTokenExpired(token));
            System.out.println("  Token type: " + getTokenType(token));
        } catch (Exception e) {
            System.err.println("Cannot parse token: " + e.getMessage());
        }
    }
}