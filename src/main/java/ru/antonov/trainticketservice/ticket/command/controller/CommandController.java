package ru.antonov.trainticketservice.ticket.command.controller;

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
import ru.antonov.trainticketservice.ticket.command.command.TicketCancelPendingCommand;
import ru.antonov.trainticketservice.ticket.command.command.TicketReserveCommand;

import ru.antonov.trainticketservice.ticket.command.handler.TicketCancelPendingHandler;
import ru.antonov.trainticketservice.ticket.command.handler.TicketReservedHandler;
import ru.antonov.trainticketservice.ticket.command.dto.TicketBookDto;
import ru.antonov.trainticketservice.ticket.command.dto.TicketCancelDto;
import ru.antonov.trainticketservice.user.entity.User;

/**
 * REST-контроллер command-side части для операций изменения билетов.
 * <p>
 * Принимает пользовательские запросы и передает их command handlers,
 * которые создают события агрегата билета.
 */
@RestController
@RequestMapping("/command/tickets")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommandController {
    private final TicketCancelPendingHandler ticketCancelPendingHandler;
    private final TicketReservedHandler ticketReservedHandler;

    /**
     * Запускает асинхронную отмену билета для авторизованного пользователя.
     *
     * @param principal авторизованный пользователь
     * @param dto тело запроса на отмену
     * @return пустой ответ {@code 202 Accepted}
     */
    @Operation(
            summary = "Возврат билета",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление билетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
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
            @ApiResponse(responseCode = "409",
                    description = "CONFLICT",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "409",
                                          "message": "Данный билет уже был отменен вами"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "422",
                    description = "UNPROCESSABLE ENTITY",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "422",
                                          "message": "Нельзя вернуть билет меньше чем за 24 часа до отправления"
                                        }
                                    """)
                    ))
    }
    )
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelTicket(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid TicketCancelDto dto
    ) {
        TicketCancelPendingCommand command = TicketCancelPendingCommand.builder()
                .aggregateId(dto.getTicketId())
                .userId(principal.getId())
                .build();

        ticketCancelPendingHandler.handle(command);
        return ResponseEntity.accepted().build();
    }

    /**
     * Запускает асинхронное бронирование билета для авторизованного пользователя.
     *
     * @param principal авторизованный пользователь
     * @param dto тело запроса на бронирование
     * @return пустой ответ {@code 202 Accepted}
     */
    @Operation(
            summary = "Покупка билета",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Tag(name = "Управление билетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
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
                                          "message": "Этот билет уже был забронирован вами"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "422",
                    description = "UNPROCESSABLE ENTITY",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "422",
                                          "message": "Нельзя купить билет меньше чем за 2 часа до отправления"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "429",
                    description = "TOO MANY REQUESTS",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "429",
                                          "message": "TOO MANY REQUESTS"
                                        }
                                    """)
                    ))
    }
    )
    @PostMapping("/book")
    public ResponseEntity<Void> bookTicket(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody TicketBookDto dto
    ) {
        TicketReserveCommand command = TicketReserveCommand.builder()
                .userId(principal.getId())
                .seatId(dto.getSeatId())
                .stopFromId(dto.getStopFromId())
                .stopToId(dto.getStopToId())
                .build();

        ticketReservedHandler.handle(command);
        return ResponseEntity.accepted().build();
    }
}
