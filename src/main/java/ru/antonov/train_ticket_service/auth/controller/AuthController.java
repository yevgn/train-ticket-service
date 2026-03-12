package ru.antonov.train_ticket_service.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.antonov.train_ticket_service.auth.dto.AuthRequestDto;
import ru.antonov.train_ticket_service.auth.dto.AuthResponseDto;
import ru.antonov.train_ticket_service.auth.dto.RefreshAccessTokenRequestDto;
import ru.antonov.train_ticket_service.auth.service.AuthService;
import ru.antonov.train_ticket_service.user.entity.User;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Аутентификация пользователя")
    @Tag(name = "Аутентификация. Управление токенами, сессиями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401",
                    description = "Неуспешная аутентификация (введены неверные данные/аккаунт не активирован/аккаунт" +
                            " заблокирован)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "401",
                                          "message": "Ошибка аутентификации. Введеные неправильные данные"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос (например, неверные данные или данные отсутствуют)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Поле email не может быть пустым"
                                        }
                                    """)
                    ))
    }
    )
    @PostMapping("/make-auth")
    public ResponseEntity<AuthResponseDto> authenticate(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(
            summary = "Обновление токена доступа",
            description = "Токен доступа можно обновить по refresh токену"
    )
    @Tag(name = "Аутентификация. Управление токенами, сессиями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное обновление токена доступа"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос (например, неправильно сконфигурирован токен/токен истек/" +
                            "токен отсутствует)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Refresh токен истек или неправильно сконфигурирован"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "403",
                    description = "Нет доступа. Например, аккаунт заблокирован/аккаунт не активирован",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Ошибка. Ваш аккаунт заблокирован. Обратитесь в службу поддержки"
                                        }
                                    """)
                    ))
    }
    )
    @PostMapping("/refresh-access-token")
    public ResponseEntity<AuthResponseDto> refreshAccessToken(@Valid @RequestBody RefreshAccessTokenRequestDto request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    @Operation(
            summary = "Завершение сессии",
            description = "Инвалидация access и refresh токенов пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Аутентификация. Управление токенами, сессиями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное завершение сессии"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет доступа"
            )
    }
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User user) {
        authService.logout(user);
        return ResponseEntity.ok().build();
    }
}