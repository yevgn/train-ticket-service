package ru.antonov.trainticketservice.cruise.controller;

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
import ru.antonov.trainticketservice.cruise.dto.CruiseFilterRequestDto;
import ru.antonov.trainticketservice.cruise.dto.CruiseResponseDto;
import ru.antonov.trainticketservice.cruise.dto.CruiseShortResponseDto;
import ru.antonov.trainticketservice.cruise.dto.CruiseStopUpdateRequestDto;
import ru.antonov.trainticketservice.cruise.service.CruiseService;
import ru.antonov.trainticketservice.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cruises")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CruiseController {
    private final CruiseService cruiseService;

    @Operation(
            summary = "Получение информации о всех рейсах",
            description = "Есть возможность фильтрации рейсов по нескольким параметрам"
    )
    @Tag(name = "Управление рейсами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Поле date не может отсутствовать"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/batch/filter")
    public ResponseEntity<List<CruiseShortResponseDto>> findAllCruisesWithFilter(
            @Valid @RequestBody CruiseFilterRequestDto request
    ) {
        return ResponseEntity.ok(
                cruiseService.findAllCruisesWithFilter( request)
        );
    }

    @Operation(
            summary = "Получение информации о всех рейсах"
    )
    @Tag(name = "Управление рейсами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500",
                    description = "Ошибка на сервере",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "500",
                                          "message": "Ошибка на сервере"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/batch")
    public ResponseEntity<List<CruiseShortResponseDto>> findAllCruises() {
        return ResponseEntity.ok(
                cruiseService.findAllCruises()
        );
    }

    @Operation(
            summary = "Получение информации о конкретном рейсе по id"
    )
    @Tag(name = "Управление рейсами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Рейс с таким id не найден"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/{cruiseId}")
    public ResponseEntity<CruiseResponseDto> findById(@PathVariable UUID cruiseId) {
        return ResponseEntity.ok(
                cruiseService.findCruiseById(cruiseId)
        );
    }

    @Operation(
            summary = "Изменение информации о рейсе",
            description = "Требуется роль ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление рейсами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Данного рейса не существует"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "401",
                                          "message": "Вы не авторизованы"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Доступ запрещен"
                                        }
                                    """)
                    ))
    }
    )
    @PatchMapping("/{cruiseId}/stops/{stopId}")
    public ResponseEntity<?> updateCruiseStop(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID cruiseId,
            @PathVariable UUID stopId,
            @Valid @RequestBody CruiseStopUpdateRequestDto request
    ) {
        cruiseService.updateCruiseStop(principal, cruiseId, stopId, request);
        return ResponseEntity.ok().build();
    }
}
