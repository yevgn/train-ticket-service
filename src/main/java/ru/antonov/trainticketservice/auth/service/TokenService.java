package ru.antonov.trainticketservice.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.trainticketservice.auth.entity.Token;
import ru.antonov.trainticketservice.auth.entity.TokenMode;
import ru.antonov.trainticketservice.auth.entity.TokenType;
import ru.antonov.trainticketservice.auth.repository.TokenRepository;
import ru.antonov.trainticketservice.user.entity.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final TokenRepository tokenRepository;

    public Optional<User> findUserByToken(String token){
        return tokenRepository.findUserByToken(token);
    }

    public Token saveToken(String token, TokenType tokenType, TokenMode tokenMode, User user) {
        Token tokenEntity = Token.builder()
                .token(token)
                .tokenMode(tokenMode)
                .tokenType(tokenType)
                .user(user)
                .build();
        return tokenRepository.saveAndFlush(tokenEntity);
    }

    public int revokeTokensByUserEmailAndTokenModeIn(String email, List<TokenMode> modes){
        return tokenRepository.revokeAllByUserEmailAndTokenModeIn(email, modes);
    }

}
