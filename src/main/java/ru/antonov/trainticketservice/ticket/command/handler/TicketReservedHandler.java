package ru.antonov.trainticketservice.ticket.command.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.antonov.trainticketservice.common.exception.*;
import ru.antonov.trainticketservice.cruise.entity.Cruise;
import ru.antonov.trainticketservice.cruise.entity.CruiseFare;
import ru.antonov.trainticketservice.cruise.entity.Stop;
import ru.antonov.trainticketservice.cruise.service.CruiseFareService;
import ru.antonov.trainticketservice.cruise.service.CruiseService;
import ru.antonov.trainticketservice.cruise.service.StopService;
import ru.antonov.trainticketservice.seat.entity.Seat;
import ru.antonov.trainticketservice.seat.serivce.SeatService;
import ru.antonov.trainticketservice.ticket.command.aggregate.TicketAggregate;
import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.command.TicketReserveCommand;
import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;


import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketReservedEventData;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;
import ru.antonov.trainticketservice.user.entity.User;
import ru.antonov.trainticketservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Обрабатывает команды резервирования билета.
 * <p>
 * Проверяет маршрут, место, тариф, владельца и временные ограничения,
 * после чего добавляет событие резервирования в агрегат билета.
 */
@Component
@Slf4j
public class TicketReservedHandler extends HandlerRoot<TicketAggregate> {
    private final StopService stopService;
    private final SeatService seatService;
    private final CruiseFareService cruiseFareService;
    private final UserService userService;

    private final int MAX_HOURS_BEFORE_TO_RESERVE_TICKET = 2;

    public TicketReservedHandler(
            EventRepository eventRepository,
            EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox,
            EventDataMapper eventDataMapper,
            SeatService seatService,
            StopService stopService,
            CruiseFareService cruiseFareService,
            UserService userService
    ) {
        super(eventRepository, eventSourcedTicketRepositoryWithOutbox, eventDataMapper, TicketAggregate::new);
        this.seatService = seatService;
        this.stopService = stopService;
        this.cruiseFareService = cruiseFareService;
        this.userService = userService;
    }

    private void validateStopOrder(TicketReserveCommand command, Stop from, Stop to, UUID userId) {
        if (from.getStopOrder() > to.getStopOrder()) {
            throw new InvalidStopOrderException(
                    "Ошибка. Некорректно указаны поля \"от\" и \"до\"",
                    String.format("Ошибка при покупке билета на место %s от %s до %s пользователем %s: неправильно указан" +
                            " порядок остановок", command.getSeatId(), from.getId(), to.getId(), userId),
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateStops(Stop from, Stop to, UUID userId){
        if (!from.getCruise().equals(to.getCruise())) {
            throw new BadRequestException(
                    "Остановки принадлежат разным рейсам",
                    String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки принадлежат" +
                            " разным рейсам", from.getId(), to.getId(), userId),
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    /**
     * Создает событие резервирования на основе запроса после доменной валидации.
     *
     * @param aggregate агрегат билета, восстановленный из истории
     * @param command команда резервирования
     */
    @Override
    public void applyEvent(TicketAggregate aggregate, Command command) {
        UUID userId = ((TicketReserveCommand) command).getUserId();
        UUID seatId = ((TicketReserveCommand) command).getSeatId();

        UUID stopFromId = ((TicketReserveCommand) command).getStopFromId();
        UUID stopToId = ((TicketReserveCommand) command).getStopToId();

        // проверить действительно ли существуют остановки
        Stop from = stopService.findByIdWithStation(stopFromId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Остановки \"от\" не существует",
                                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки " +
                                                " %s не существует",
                                        stopFromId, stopToId, userId, stopFromId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        Stop to = stopService.findByIdWithStation(stopToId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Остановки \"до\" не существует",
                                String.format("Ошибка при покупке билета от %s до %s для пользователя %s: остановки " +
                                                " %s не существует",
                                        stopFromId, stopToId, userId, stopToId),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        // проверить, относятся ли остановки к одному и тому же рейсу
        validateStops(from, to, userId);

        // в правильном ли порядке указаны остановки
        validateStopOrder((TicketReserveCommand) command, from, to, userId);

        // проверить владельца и статус билета
        validateOwnerAndStatus(aggregate, (TicketReserveCommand) command, userId);

        Cruise cruise = from.getCruise();

        // ПРОВЕРИТЬ соответствие поезда к которому привязан рейс и поезда к которому привязано место
        Seat seat = seatService.findByIdWithCategoryAndCarriage(((TicketReserveCommand) command).getSeatId())
                .filter(s -> s.getCarriage().getTrain().equals(cruise.getTrain()))
                .orElseThrow(
                        () -> new BadRequestException(
                                "Ошибка. Данного места для этого рейса не существует",
                                String.format("Ошибка при покупке билета на место %s от %s до %s пользователем %s:" +
                                                " данного места не существует", seatId,
                                        stopFromId, stopToId, command
                                ),
                                ErrorCode.ENTITY_NOT_FOUND
                        )
                );

        // загрузка тарифа для этого рейса и места
        CruiseFare cruiseFare = cruiseFareService.findByParamsFetchAllFields(
                cruise.getId(), seat.getCategory().getId(), seat.getCarriage().getCategory().getId()
        ).orElseThrow(() -> new InvalidDataStateException(
                "Ошибка на сервере",
                String.format("Ошибка при покупке билета на место %s от %s до %s пользователем %s: не указан тариф для" +
                        " места", seat.getId(), stopFromId, stopToId, userId
                ),
                ErrorCode.INVALID_DATA_STATE
        ));

        User owner = userService.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Пользователя, от лица которого вы пытаетесь купить билет, не существует",
                        String.format("Ошибка при покупке билета на место %s от %s до %s: " +
                                " пользователь %s был удален", seat.getId(), stopFromId, stopToId, userId
                        ),
                        ErrorCode.UNPROCESSABLE_ENTITY
                )
        );

        TicketReservedEventData data = TicketReservedEventData.builder()
                .aggregateId(aggregate.getId())
                .ownerId(userId)
                .ownerSurname(owner.getSurname())
                .ownerName(owner.getName())
                .ownerPatronymic(owner.getPatronymic())
                .ownerEmail(owner.getEmail())
                .cruiseId(cruise.getId())
                .cruiseNumber(cruise.getNumber())
                .carriageId(seat.getCarriage().getId())
                .carriageCategory(seat.getCarriage().getCategory().getCategory().name())
                .carriageNumber(seat.getCarriage().getNumber())
                .seatId(seatId)
                .seatNumber(seat.getNumber())
                .seatCategory(seat.getCategory().getCategory().name())
                .fromStopId(stopFromId)
                .fromLocation(from.getStation().getLocation())
                .fromStation(from.getStation().getName())
                .toStopId(stopToId)
                .toLocation(to.getStation().getLocation())
                .toStation(to.getStation().getName())
                .fare(cruiseFare.getBaseFare())
                .reservedAt(LocalDateTime.now())
                .departureTime(from.getPlannedDeparture())
                .arrivalTime(to.getActualArrival())
                .paymentIdempotencyKey(generatePaymentIdempotencyKey())
                .build();

        // вызов агрегата
        aggregate.reserveTicket(data);
    }

    private void validateOwnerAndStatus(TicketAggregate aggregate, TicketReserveCommand command, UUID userId) {
        if (aggregate.getStatus() == null) return;
        boolean isSameUser = userId.equals(aggregate.getOwnerId());
        boolean isNotCancelled = aggregate.getStatus() != TicketAggregate.Status.CANCELLED;

        if (!isSameUser && isNotCancelled) {
            throw new TicketOfOtherUserException(
                    "Ошибка доступа. Данный билет принадлежит другому пользователю",
                    String.format("TicketOfOtherUserException при бронировании места %s, от %s до %s " +
                                    "пользователем %s. Билет с такими параметрами %s уже зарезервирован для " +
                                    "пользователя %s:", command.getSeatId(),
                            command.getStopFromId(), command.getStopToId(), userId, aggregate.getId(), aggregate.getOwnerId()),
                    ErrorCode.DATA_CONFLICT
            );
        } else if (isSameUser && aggregate.getStatus() == TicketAggregate.Status.RESERVED) {
            throw new TicketAlreadyReservedException(
                    "Запрос на бронирование билета уже находится в обработке",
                    String.format("TicketAlreadyReservedException при бронировании места %s, от %s до %s " +
                                    "пользователем %s. Билет %s уже находится в обработке", command.getSeatId(),
                            command.getStopFromId(), command.getStopToId(), userId, aggregate.getId()),
                    ErrorCode.ACCEPTED
            );
        } else if (isSameUser && aggregate.getStatus() == TicketAggregate.Status.BOOKED) {
            throw new TicketAlreadyBookedException(
                    "Этот билет уже забронирован вами",
                    String.format("TicketAlreadyBookedException при бронировании места %s, от %s до %s " +
                                    "пользователем %s. Билет %s уже забронирован для этого пользователя",
                            command.getSeatId(), command.getStopFromId(), command.getStopToId(), userId, aggregate.getId()),
                    ErrorCode.DATA_CONFLICT
            );
        } else if (isSameUser && aggregate.getStatus() == TicketAggregate.Status.CANCEL_PENDING) {
            throw new TicketAlreadyCancellingException(
                    "Ошибка бронирования билета. Система выполняет операцию возврата билета",
                    String.format("TicketAlreadyCancellingException при бронировании места %s, от %s до %s " +
                                    "пользователем %s. Билет - %s",
                            command.getSeatId(), command.getStopFromId(), command.getStopToId(), userId, aggregate.getId()),
                    ErrorCode.DATA_CONFLICT
            );
        }

        boolean canBook = LocalDateTime
                .now()
                .plusHours(MAX_HOURS_BEFORE_TO_RESERVE_TICKET)
                .isBefore(aggregate.getDepartureTime());

        if (!canBook) {
            throw new TicketReserveTimeValidationException(
                    "Покупка билетов на рейс невозможна. Слишком поздно",
                    String.format("Ошибка при покупке билета на место %s от %s до %s пользователем %s: " +
                                    "до начала рейса осталось меньше %s часов", command.getSeatId(),
                            command.getStopFromId(), command.getStopToId(), userId,
                            MAX_HOURS_BEFORE_TO_RESERVE_TICKET
                    ),
                    ErrorCode.UNPROCESSABLE_ENTITY
            );
        }
    }

    private UUID generatePaymentIdempotencyKey() {
        return UUID.randomUUID();
    }
}
