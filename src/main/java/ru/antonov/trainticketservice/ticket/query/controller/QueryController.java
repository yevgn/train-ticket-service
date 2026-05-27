package ru.antonov.trainticketservice.ticket.query.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.antonov.trainticketservice.ticket.query.dto.TicketDto;
import ru.antonov.trainticketservice.ticket.query.dto.TicketShortDto;
import ru.antonov.trainticketservice.ticket.query.service.TicketQueryService;
import ru.antonov.trainticketservice.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/query/tickets")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QueryController {
    private final TicketQueryService queryService;

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
                    description = "Нет доступа",
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
    @GetMapping("/user/{userId}/ticket-history")
    public ResponseEntity<List<TicketShortDto>> findUserTicketHistory(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                queryService.findTicketsByUserId(userId, principal)
        );
    }

    @Operation(
            summary = "Поиск информации о билете по id",
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
                                          "message": "Нет доступа"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDto> findTicketById(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID ticketId
    ) {
        return ResponseEntity.ok(
                queryService.findTicketById(ticketId, principal)
        );
    }
}
