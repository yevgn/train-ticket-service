package ru.antonov.train_ticket_service.ticket_purchase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ru.antonov.train_ticket_service.common.exception.*;
import ru.antonov.train_ticket_service.ticket_purchase.dto.DtoFactory;
import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketPurchaseRequestDto;
import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.dto.TicketShortResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.entity.*;
import ru.antonov.train_ticket_service.ticket_purchase.message_producer.ticket_purchased.TicketPurchasedProducer;
import ru.antonov.train_ticket_service.ticket_purchase.repository.TicketRepository;
import ru.antonov.train_ticket_service.user.entity.Role;
import ru.antonov.train_ticket_service.user.entity.User;
import ru.antonov.train_ticket_service.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final StopService stopService;
    private final SeatService seatService;
    private final UserService userService;
    private final CruiseFareService cruiseFareService;

    private final TicketAsyncService ticketAsyncService;

    private final Integer maxHoursForSeatsPurchaseBeforeCruiseStart = 2;
    private final Integer maxHoursBeforeDepartureToReturnTicket = 24;

    public Optional<Ticket> findById(UUID ticketId) {
        return ticketRepository.findById(ticketId);
    }

    public Optional<Ticket> findByIdWithCruiseAndTargetStopsAndUser(UUID ticketId){
        return ticketRepository.findByIdWithCruiseAndTargetStopsAndUser(ticketId);
    }

    public TicketResponseDto purchaseTicket(User principal, TicketPurchaseRequestDto request) {
        UUID stopFromId = request.getStopFromId();
        UUID stopToId = request.getStopToId();

        // проверить действительно ли существуют остановки
        Stop from = stopService.findByIdWithStation( request.getStopFromId())
                .orElseThrow(
                        () -> new EntityNotFoundEx(
                                "Остановки \"от\" не существует",
                                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки " +
                                                " %s не существует",
                                       stopFromId, stopToId, principal, stopFromId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop to = stopService.findByIdWithStation( request.getStopToId())
                .orElseThrow(
                        () -> new EntityNotFoundEx(
                                "Остановки \"до\" не существует",
                                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки " +
                                                " %s не существует",
                                        stopFromId, stopToId, principal, stopToId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        // проверить, относятся ли остановки к одному и тому же рейсу
        if(!from.getCruise().equals(to.getCruise())){
            throw new BadRequestEx(
                    "Остановки принадлежат разным рейсам",
                    String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки принадлежат" +
                                    " разным рейсам", stopFromId, stopToId, principal),
                    ErrorCode.BAD_REQUEST
            );
        }

        // в правильном ли порядке указаны остановки
        if (!isValidStopOrder(from, to)) {
            throw new BadRequestEx(
                    "Неправильно указаны остановки",
                    String.format("Ошибка при покупке билета от %s до %s для пользователя %s: неправильно указан" +
                            " порядок остановок", stopFromId, stopToId, principal),
                    ErrorCode.BAD_REQUEST
            );
        }

        Cruise cruise = from.getCruise();

        // ПРОВЕРИТЬ соответствие поезда к которому привязан рейс и поезда к которому привязано место
        Seat seat = seatService.findByIdWithCategoryAndCarriage(request.getSeatId())
                .filter(s -> s.getCarriage().getTrain().equals(cruise.getTrain()))
                .orElseThrow(
                        () -> new BadRequestEx(
                                "Данного места для этого рейса не существует",
                                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: места %s" +
                                        " для данного рейса не существует", stopFromId, stopToId, principal,
                                        request.getSeatId()
                                ),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        if (!isSeatsAvailableForPurchaseByCruiseId(principal, cruise)) {
            throw new ResourceAccessDeniedEx(
                    "Покупка билетов на рейс невозможна. Слишком поздно",
                    String.format("Ошибка при покупке билета от %s до %s для пользователя %s: до начала рейса" +
                                    " осталось меньше %s часов", stopFromId, stopToId, principal,
                            maxHoursForSeatsPurchaseBeforeCruiseStart
                    ),
                    ErrorCode.FORBIDDEN
            );
        }

        // загрузка тарифа для этого рейса и места
        CruiseFare cruiseFare = cruiseFareService.findByParamsFetchAllFields(
                cruise.getId(), seat.getCategory().getId(), seat.getCarriage().getCategory().getId()
        ).orElseThrow(() -> new InvalidDataStateEx(
                "Ошибка на сервере",
                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: не указан тариф для" +
                                " места %s", stopFromId, stopToId, principal, seat.getId()
                ),
                ErrorCode.INVALID_DATA_STATE
        ));

        // добавить инфу в БД
        Ticket ticket = Ticket
                .builder()
                .seat(seat)
                .purchasedAt(LocalDateTime.now())
                .cruise(cruise)
                .status(Ticket.Status.PURCHASED)
                .user(principal)
                .from(from)
                .to(to)
                .actualFare(calculateActualFare(cruiseFare.getBaseFare()))
                .build();

        save(principal, ticket);

        Stop cruiseStartStop = stopService.findFirstStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует первая остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );
        Stop cruiseEndStop = stopService.findLastStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует последняя остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );

        ticketAsyncService.sendTicketPurchasedEvent(ticket, cruiseStartStop, cruiseEndStop, principal);

        return DtoFactory.makeTicketResponseDto(ticket, cruiseStartStop, cruiseEndStop);
    }

    // в бд висит ограничение на пересечение билетов по станциям
    public Ticket save(User principal, Ticket ticket) {
        try {
            return ticketRepository.save(ticket);
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictEx(
                    "Место уже занято на выбранном участке",
                    String.format("Ошибка покупки места %s на рейс %s от %s до %s пользователем %s: место занято" +
                                    " на выбранном участке", ticket.getSeat().getId(), ticket.getCruise().getId(),
                            ticket.getFrom().getStation().getName(), ticket.getTo().getStation().getName(), principal.getId()),
                    ErrorCode.DATA_CONFLICT
            );
        }
    }

    public boolean isTicketExistsByCruiseIdAndSeatId(UUID cruiseId, UUID seatId) {
        return ticketRepository.isTicketExistsByCruiseIdAndSeatId(cruiseId, seatId);
    }

    private boolean isValidStopOrder(Stop from, Stop to) {
        return from.getStopOrder() < to.getStopOrder();
    }

    // для простоты actualFare = baseFare
    private float calculateActualFare(Float baseFare) {
        return baseFare + 0.0f;
    }

    public boolean isSeatsAvailableForPurchaseByCruiseId(User principal, Cruise cruise) {
        // посмотреть дату первой остановки, если до начала рейса меньше N (указано в поле) часов - нельзя
        Optional<Stop> stopOpt = stopService.findFirstStopByCruiseId(cruise.getId());
        if (stopOpt.isEmpty()) {
            throw new InvalidDataStateEx(
                    "Ошибка на сервере",
                    String.format("Ошибка при попытке купить билет на рейс %s пользователем %s: нет" +
                            " остановок", cruise.getId(), principal.getId()),
                    ErrorCode.INVALID_DATA_STATE
            );
        }

        return LocalDateTime
                .now()
                .plusHours(maxHoursForSeatsPurchaseBeforeCruiseStart)
                .isBefore(stopOpt.get().getPlannedArrival());
    }

    public List<TicketShortResponseDto> findUserTicketPurchaseHistory(User principal, UUID userId) {
        User other = userService.findById(userId).orElseThrow(() -> new EntityNotFoundEx(
                "Пользователь не найден",
                String.format("Ошибка получения истории покупок пользователя %s пользователем %s: пользователь не найден",
                        userId, principal.getId()),
                ErrorCode.ENTITY_NOT_FOUND
        ));

        checkUserHasAccessToOtherPurchaseHistoryElseThrow(principal, other);

        return ticketRepository.findAllByUserIdWithCruiseAndTargetStops(userId)
                .stream()
                .map(DtoFactory::makeTicketShortResponseDto)
                .toList();
    }

    private void checkUserHasAccessToOtherPurchaseHistoryElseThrow(User principal, User other) {
        if (principal.getRole() == Role.ADMIN) return;

        else if (!principal.equals(other)) {
            throw new ResourceAccessDeniedEx(
                    "Нет доступа",
                    String.format("Ошибка при попытке просмотреть историю покупок пользователя %s пользователем %s:" +
                            "нет доступа", other.getId(), principal.getId()),
                    ErrorCode.FORBIDDEN
            );
        }
    }

    public void returnTicket(User principal, UUID ticketId) {
        Ticket ticket = findByIdWithCruiseAndTargetStopsAndUser(ticketId).orElseThrow(
                () -> new EntityNotFoundEx(
                        "Билет не найден",
                        String.format("Ошибка при сдаче билета %s пользователем %s: билет не найден",
                                ticketId, principal.getId()),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        // проверка на принадлежность билета этому юзеру
        if (!ticket.getUser().equals(principal)) {
            throw new ResourceAccessDeniedEx(
                    "Ошибка. Нет прав. Данный билет принадлежит другому пользователю",
                    String.format("Ошибка при сдаче билета %s пользователем %s: билет принадлежит другому пользователю",
                            ticketId, principal.getId()),
                    ErrorCode.FORBIDDEN
            );
        }

        if(ticket.getStatus() == Ticket.Status.RETURNED){
            throw new BadRequestEx(
                    "Ошибка. Билет уже сдан",
                    String.format("Ошибка при сдаче билета %s пользователем %s: билет уже сдан", ticketId, principal.getId()),
                    ErrorCode.BAD_REQUEST
            );
        }

        // проверка на возможность вернуть билет (24 часа)
        if (!isCanReturnTicket(ticket)) {
            throw new BadRequestEx(
                    "Ошибка. Билет вернуть нельзя",
                    String.format("Ошибка при сдаче билета %s пользователем %s: либо меньше 24 часов до отправления, либо" +
                                    " рейс %s завершен или отменен",
                            ticketId, principal.getId(), ticket.getCruise().getId()),
                    ErrorCode.BAD_REQUEST
            );
        }

        ticket.setStatus(Ticket.Status.RETURNED);
        ticket.setReturnedAt(LocalDateTime.now());

        save(principal, ticket);

        ticketAsyncService.findCruiseStopsAndSendTicketReturnedEvent(ticket);
    }

    private boolean isCanReturnTicket(Ticket ticket) {
        LocalDateTime departureTime = ticket.getFrom().getPlannedDeparture();
        return (ticket.getCruise().getStatus() == Cruise.Status.PLANNED ||
                ticket.getCruise().getStatus() == Cruise.Status.ONGOING ) &&
                LocalDateTime.now().plusHours(maxHoursBeforeDepartureToReturnTicket).isBefore(departureTime);
    }

}
