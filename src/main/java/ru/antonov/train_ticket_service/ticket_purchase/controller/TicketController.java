package ru.antonov.train_ticket_service.ticket_purchase.controller;

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

import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketPurchaseRequestDto;
import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketShortResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.service.TicketService;
import ru.antonov.train_ticket_service.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TicketController {
    private final TicketService ticketService;

    @Operation(
            summary = "Возврат билета",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление билетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Билет не найден"
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
                    description = "Нет доступа",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Ошибка. Нет прав. Данный билет принадлежит другому пользователю"
                                        }
                                    """)
                    )),
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
    @PostMapping("/{ticketId}/return")
    public ResponseEntity<?> returnTicket(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID ticketId
    ) {
        ticketService.returnTicket(principal, ticketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Покупка билета",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление билетами")
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
                    description = "Нет доступа",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Покупка билетов на рейс невозможна"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "409",
                    description = "Конфликт данных",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "409",
                                          "message": "Это место было уже было выкуплено"
                                        }
                                    """)
                    )),
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
    @PostMapping
    public ResponseEntity<TicketResponseDto> purchaseTicket(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody TicketPurchaseRequestDto request
    ) {
        return ResponseEntity.ok(
                ticketService.purchaseTicket(principal, request)
        );
    }

    @Operation(
            summary = "Истории покупок конкретного пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление билетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Пользователь не найден"
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
                    description = "Нет доступа (когда пользователь с ролью USER пытается посмотреть историю покупок" +
                            " другого пользователя с ролью USER)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "403",
                                          "message": "Нет доступа"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/user/{userId}/purchase-history")
    public ResponseEntity<List<TicketShortResponseDto>> findUserTicketPurchaseHistory(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID userId
            ){
        return ResponseEntity.ok(
                ticketService.findUserTicketPurchaseHistory(principal, userId)
        );
    }
}