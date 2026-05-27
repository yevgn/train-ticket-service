package ru.antonov.trainticketservice.seat.serivce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.trainticketservice.common.exception.BadRequestException;
import ru.antonov.trainticketservice.common.exception.EntityNotFoundException;
import ru.antonov.trainticketservice.common.exception.ErrorCode;
import ru.antonov.trainticketservice.seat.dto.SeatResponseDto;
import ru.antonov.trainticketservice.cruise.entity.Cruise;
import ru.antonov.trainticketservice.seat.entity.Seat;
import ru.antonov.trainticketservice.cruise.entity.Stop;
import ru.antonov.trainticketservice.seat.repository.SeatRepository;
import ru.antonov.trainticketservice.cruise.service.CruiseService;
import ru.antonov.trainticketservice.cruise.service.StopService;
import ru.antonov.trainticketservice.ticket.query.dto.OccupiedSeatDto;
import ru.antonov.trainticketservice.ticket.query.service.OccupiedSeatQueryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {
    private final SeatRepository seatRepository;

    private final CruiseService cruiseService;
    private final StopService stopService;
    private final OccupiedSeatQueryService occupiedSeatsQueryService;

    public Optional<Seat> findById(UUID seatId) {
        return seatRepository.findById(seatId);
    }

    public Optional<Seat> findByIdWithCategoryAndCarriage(UUID seatId) {
        return seatRepository.findByIdWithCategoryAndCarriage(seatId);
    }

    public List<SeatResponseDto> findAvailableSeatsByCruiseIdAndTargetStops(
            UUID cruiseId, UUID fromStopId, UUID toStopId
    ) {

        Cruise cruise = cruiseService.findById(cruiseId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Данного рейса не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " рейса не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop fromStop = stopService.findById(fromStopId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Остановки \"от\" не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " остановки от не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop toStop = stopService.findById(toStopId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Остановки \"до\" не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " остановки до не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        // валидация
        validateCruiseHasTargetStops(fromStop, toStop, cruise);
        validateStopOrder(fromStop, toStop, cruiseId);

        // места на рейс (ВСЕ)
        List<SeatResponseDto> seats = seatRepository.findAllByCruiseId(cruiseId);

        // занятые из READ MODEL
        List<OccupiedSeatDto> occupiedSeats = occupiedSeatsQueryService.findOccupiedSeatsByCruiseId(cruiseId);

        // исключение занятых мест
        return excludeOccupied(seats, occupiedSeats, fromStop.getStopOrder(), toStop.getStopOrder());
    }

    private List<SeatResponseDto> excludeOccupied(
            List<SeatResponseDto> seats,
            List<OccupiedSeatDto> occupied,
            int targetFromOrder,
            int targetToOrder
    ) {
        Map<UUID, List<OccupiedSeatDto>> occupiedBySeat = occupied.stream()
                .collect(Collectors.groupingBy(OccupiedSeatDto::getSeatId));

        return seats.stream()
                .filter(seat -> !isOccupiedOnSegment(
                        occupiedBySeat.getOrDefault(seat.getId(), List.of()),
                        targetFromOrder,
                        targetToOrder
                ))
                .toList();
    }

    private boolean isOccupiedOnSegment(
            List<OccupiedSeatDto> occupiedSegments,
            int targetFrom,
            int targetTo
    ) {
        return occupiedSegments.stream()
                .anyMatch(o -> segmentsOverlap(
                        o.getFromOrder(), o.getToOrder(),
                        targetFrom, targetTo
                ));
    }

    private boolean segmentsOverlap(int fromA, int toA, int fromB, int toB) {
        return fromA < toB && fromB < toA;
    }

    private void validateCruiseHasTargetStops(Stop from, Stop to, Cruise cruise) {
        if (!from.getCruise().equals(cruise) || !to.getCruise().equals(cruise)) {
            throw new BadRequestException(
                    "У указанного рейса нет заданных остановок",
                    String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                    " у указанного рейса нет заданных остановок",
                            cruise.getId(), from.getId(), to.getId()),
                    ErrorCode.ENTITY_NOT_FOUND
            );
        }
    }

    private void validateStopOrder(Stop from, Stop to, UUID cruiseId) {
        if (from.getStopOrder() >= to.getStopOrder()) {
            throw new BadRequestException(
                    "Неправильно указан порядок остановок",
                    String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                    " неправильно указан порядок остановок. fromStopOrder - %s, toStopOrder - %s",
                            cruiseId, from.getId(), to.getId(), from.getStopOrder(),
                            to.getStopOrder()
                    ),
                    ErrorCode.BAD_REQUEST
            );
        }
    }
}
