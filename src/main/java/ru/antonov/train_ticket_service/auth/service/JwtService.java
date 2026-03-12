package ru.antonov.train_ticket_service.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.antonov.train_ticket_service.auth.entity.TokenMode;
import ru.antonov.train_ticket_service.auth.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import ru.antonov.train_ticket_service.common.exception.ConfigPropertiesNotSetEx;
import ru.antonov.train_ticket_service.common.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    @Value("${spring.application.jwt.secret-key}")
    private String secretKey;
    @Value("${spring.application.jwt.user-access-token-expiration}")
    private Long userAccessTokenExpiration;
    @Value("${spring.application.jwt.user-refresh-token-expiration}")
    private Long userRefreshTokenExpiration;
    @Value("${spring.application.name}")
    private String issuer;

    private final TokenRepository tokenRepository;

    public String extractUsername(String token) throws JwtException, IllegalArgumentException{
        return extractClaim(token, Claims::getSubject);
    }
    public String extractIssuedAt(String token) throws JwtException, IllegalArgumentException{
        return extractClaim(token, Claims::getIssuedAt).toString();
    }
    public String extractExpiredAt(String token) throws JwtException, IllegalArgumentException{
        return extractClaim(token, Claims::getExpiration).toString();
    }
    public String extractIssuer(String token) throws JwtException, IllegalArgumentException{
        return extractClaim(token, Claims::getIssuer);
    }
    public List<String> extractRoles(String token) throws JwtException, IllegalArgumentException {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public String generateUserToken(
            Set<String> roles,
            String email,
            TokenMode tokenMode
    ){
        Map<String, Object> extraClaims = new HashMap<>(Map.of("roles", roles));

        Long expiration = 0L;
        if(tokenMode.equals(TokenMode.ACCESS)){
            expiration = userAccessTokenExpiration;
        }
        else if(tokenMode.equals(TokenMode.REFRESH)){
            expiration = userRefreshTokenExpiration;
        } else{
            throw new ConfigPropertiesNotSetEx(
                    "Ошибка на сервере",
                    String.format("Ошибка на сервере. Не установлен срок годности токена с mode: %s", tokenMode.name()),
                    ErrorCode.CONFIG_PROPERTIES_NOT_SET
            );
        }

        return buildToken(
                extraClaims,
                email,
                expiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String email,
            long expiration
    ){
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .issuer(issuer)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenValidPriv(String token) throws JwtException, IllegalArgumentException{
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) throws JwtException, IllegalArgumentException {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
            throws JwtException, IllegalArgumentException{
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException{
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, TokenMode tokenMode) {
        boolean isValid = false;

        try {
            isValid = isTokenValidPriv(token);
        } catch (JwtException | IllegalArgumentException ex){
            return false;
        }

        Boolean isNotRevokedAndNotExpired = tokenRepository
                .findByToken(token)
                .filter(t -> t.getTokenMode().equals(tokenMode))
                .map(t -> !t.isRevoked() && !t.isExpired())
                .orElse(false);

        return isValid && isNotRevokedAndNotExpired;
    }
}
