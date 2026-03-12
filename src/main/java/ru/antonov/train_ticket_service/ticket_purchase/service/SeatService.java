package ru.antonov.train_ticket_service.ticket_purchase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.train_ticket_service.common.exception.BadRequestEx;
import ru.antonov.train_ticket_service.common.exception.EntityNotFoundEx;
import ru.antonov.train_ticket_service.common.exception.ErrorCode;
import ru.antonov.train_ticket_service.ticket_purchase.dto.SeatResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Seat;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;
import ru.antonov.train_ticket_service.ticket_purchase.repository.SeatRepository;
import ru.antonov.train_ticket_service.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {
    private final SeatRepository seatRepository;
    private final CruiseService cruiseService;
    private final StopService stopService;

    private final Integer maxHoursForSeatsPurchaseBeforeCruiseStart = 2;

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
                        () -> new EntityNotFoundEx(
                                "Данного рейса не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " рейса не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop fromStop = stopService.findById(fromStopId)
                .orElseThrow(
                        () -> new EntityNotFoundEx(
                                "Остановки \"от\" не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " остановки от не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop toStop = stopService.findById(toStopId)
                .orElseThrow(
                        () -> new EntityNotFoundEx(
                                "Остановки \"до\" не существует",
                                String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                                " остановки до не существует",
                                        cruiseId, fromStopId, toStopId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        if(!fromStop.getCruise().equals(cruise) || !toStop.getCruise().equals(cruise)){
            throw new BadRequestEx(
                    "У указанного рейса нет заданных остановок",
                    String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                    " у указанного рейса нет заданных остановок",
                            cruiseId, fromStopId, toStopId),
                    ErrorCode.ENTITY_NOT_FOUND
            );
        }
        else if(fromStop.getStopOrder() >= toStop.getStopOrder()){
            throw new BadRequestEx(
                    "Неправильно указан порядок остановок",
                    String.format("Ошибка при поиске доступных мест на рейс %s от %s до %s:" +
                                    " неправильно указан порядок остановок. fromStopOrder - %s, toStopOrder - %s",
                            cruiseId, fromStopId, toStopId, fromStop.getStopOrder(),
                            toStop.getStopOrder()
                    ),
                    ErrorCode.ENTITY_NOT_FOUND
            );
        }

        // поиск мест
        return seatRepository.findAvailableSeatsByTargetStopsSortByNumber(
                cruiseId, fromStop.getStopOrder(), toStop.getStopOrder()
        );
    }

}
