package ru.antonov.trainticketservice.seat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.antonov.trainticketservice.seat.dto.SeatResponseDto;
import ru.antonov.trainticketservice.seat.serivce.SeatService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SeatController {
    private final SeatService seatService;

    @Operation(
            summary = "Получение информации о свободных местах для конкретного рейса с остановки до остановки"
    )
    @Tag(name = "Управление местами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные/данные отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "400",
                                          "message": "Неправильно указан порядок остановок"
                                        }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    description = "Данного рейса не существует",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "status" : "404",
                                          "message": "Данного рейса не существует"
                                        }
                                    """)
                    ))
    }
    )
    @GetMapping
    public ResponseEntity<List<SeatResponseDto>> findAvailableSeatsByCruiseStopRange(
            @RequestParam("cruise_id") UUID cruiseId,
            @RequestParam("from_stop_id") UUID fromStopId,
            @RequestParam("to_stop_id") UUID toStopId
    ){
        return ResponseEntity.ok(
                seatService.findAvailableSeatsByCruiseIdAndTargetStops( cruiseId, fromStopId, toStopId)
        );
    }
}
