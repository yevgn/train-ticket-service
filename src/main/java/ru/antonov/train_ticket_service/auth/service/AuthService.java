package ru.antonov.train_ticket_service.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.antonov.train_ticket_service.auth.dto.AuthRequestDto;
import ru.antonov.train_ticket_service.auth.dto.AuthResponseDto;
import ru.antonov.train_ticket_service.auth.dto.RefreshAccessTokenRequestDto;
import ru.antonov.train_ticket_service.auth.entity.TokenMode;
import ru.antonov.train_ticket_service.auth.entity.TokenType;
import ru.antonov.train_ticket_service.common.exception.AuthenticationEx;
import ru.antonov.train_ticket_service.common.exception.EntityNotFoundEx;
import ru.antonov.train_ticket_service.common.exception.ErrorCode;
import ru.antonov.train_ticket_service.common.exception.RefreshTokenValidationFailureEx;
import ru.antonov.train_ticket_service.user.entity.User;
import ru.antonov.train_ticket_service.user.service.UserService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserService userService;

    public AuthResponseDto authenticate(AuthRequestDto request) {
        tryAuth(request.getEmail(), request.getPassword());

        var user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundEx(
                        "Ошибка аутентификации",
                        String.format("Неуспешная аутентификация. Пользователь с email %s не найден", request.getEmail()),
                        ErrorCode.ENTITY_NOT_FOUND
                ));

        tokenService.revokeTokensByUserEmailAndTokenModeIn(user.getEmail(), List.of(TokenMode.ACCESS, TokenMode.REFRESH));

        var accessToken = jwtService.generateUserToken(
                Set.of(user.getRole().name()),
                user.getEmail(),
                TokenMode.ACCESS
        );
        var refreshToken = jwtService.generateUserToken(
                Set.of(user.getRole().name()),
                user.getEmail(),
                TokenMode.REFRESH
        );

        tokenService.saveToken(accessToken, TokenType.BEARER, TokenMode.ACCESS, user);
        tokenService.saveToken(refreshToken, TokenType.BEARER, TokenMode.REFRESH, user);

        return AuthResponseDto.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();
    }

    public AuthResponseDto refreshAccessToken(RefreshAccessTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isTokenValid(refreshToken, TokenMode.REFRESH)) {
            throw new RefreshTokenValidationFailureEx(
                    "Неуспешная валидация refresh токена",
                    String.format("Ошибка при обновлении токена доступа. refresh Токен не прошел валидацию: %s",
                            refreshToken
                    ),
                    ErrorCode.REFRESH_TOKEN_VALIDATION_FAILURE
            );
        }

        User user = tokenService.findUserByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenValidationFailureEx(
                        "Неуспешная валидация refresh токена",
                        String.format("Ошибка при обновлении токена доступа. " +
                                "Пользователь по токену %s не найден", refreshToken
                        ),
                        ErrorCode.REFRESH_TOKEN_VALIDATION_FAILURE
                ));

        tokenService.revokeTokensByUserEmailAndTokenModeIn(user.getEmail(), List.of(TokenMode.ACCESS, TokenMode.REFRESH));

        String newAccessToken = jwtService.generateUserToken(
                Set.of(user.getRole().name()),
                user.getEmail(),
                TokenMode.ACCESS
        );

        tokenService.saveToken(newAccessToken, TokenType.BEARER, TokenMode.ACCESS, user);

        return AuthResponseDto
                .builder()
                .userId(user.getId())
                .accessToken(newAccessToken)
                .refreshToken("")
                .role(user.getRole())
                .build();
    }

    public void logout(User user) {
        tokenService.revokeTokensByUserEmailAndTokenModeIn(user.getEmail(), List.of(TokenMode.ACCESS, TokenMode.REFRESH));
    }

    private void tryAuth(String email, String password) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
        } catch (AuthenticationException ex) {
            throw new AuthenticationEx(
                    "Неуспешная аутентификация",
                    String.format("Неуспешная аутентификация пользователя %s", email),
                    ErrorCode.AUTH_FAILURE
            );
        }
    }

}

