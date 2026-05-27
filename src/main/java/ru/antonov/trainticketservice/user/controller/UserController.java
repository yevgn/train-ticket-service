package ru.antonov.trainticketservice.user.controller;

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
import org.springframework.web.bind.annotation.*;
import ru.antonov.trainticketservice.user.dto.UserRegisterRequestDto;
import ru.antonov.trainticketservice.user.dto.UserResponseDto;
import ru.antonov.trainticketservice.user.entity.User;
import ru.antonov.trainticketservice.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Регистрация пользователя"
    )
    @Tag(name = "Управление пользователями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные  данные или они отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Неправильный формат email"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "409",
                    description = "Пользователь уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "409",
                                          "message": "Ошибка. Пользователь с email test@gmail.com уже существует"
                                        }
                                    """)
                    ))
    }
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> addUser(
            @Valid @RequestBody UserRegisterRequestDto request
    ) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @Operation(
            summary = "Получение информации о пользователе по id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление пользователями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные  данные или они отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Неправильный формат id"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "403",
                    description = "Нет доступа (когда пользователь с ролью USER пытается получить доступ к другому " +
                            "пользователю)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Нет доступа"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "404",
                                          "message": "Пользователь не найден"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> findUserById(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.findUserById(principal, userId));
    }
}