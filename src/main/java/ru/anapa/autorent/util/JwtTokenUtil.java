package ru.anapa.autorent.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.model.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:defaultSecretKeyToBeReplacedInProductionWithAVeryLongSecretKeyWhichIsSufficientlyLong}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private long expiration;
    
    @Value("${jwt.refresh.expiration:604800000}") // Default: 7 days in milliseconds
    private long refreshExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Получение имени пользователя из токена
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Получение даты истечения срока действия из токена
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    // Получение даты создания токена
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Получение всех данных из токена
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Проверка, истек ли срок действия токена
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Генерация токена для пользователя
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }
    
    // Генерация токена с дополнительными данными пользователя
    public String generateTokenWithUserInfo(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        
        // Добавляем роли пользователя в токен
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
        
        return doGenerateToken(claims, user.getEmail());
    }
    
    // Генерация refresh токена (с более длительным сроком действия)
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateRefreshToken(claims, userDetails.getUsername());
    }

    // Создание токена - установка времени начала/окончания действия, подписи, subject (имя пользователя)
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    // Создание refresh токена с увеличенным сроком действия
    private String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + refreshExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Валидация токена
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Проверка токена на валидность без UserDetails
    public Boolean validateTokenOnly(String token) {
        try {
            // Проверяем, что токен можно распарсить и подпись валидна
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            // Проверяем срок действия
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Получение оставшегося времени жизни токена в секундах
    public long getTokenRemainingTimeInSeconds(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }
    
    // Получение конкретного claim из токена
    public Object getClaimValue(String token, String claimName) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.get(claimName);
    }
    
    // Проверка является ли пользователь администратором по токену
    public boolean isUserAdmin(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            if (claims.get("roles") instanceof java.util.List) {
                java.util.List<?> roles = (java.util.List<?>) claims.get("roles");
                return roles.contains("ROLE_ADMIN");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
} 