package ru.antonov.trainticketservice.ticket.query.projector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.*;
import ru.antonov.trainticketservice.ticket.query.entity.OccupiedSeatView;
import ru.antonov.trainticketservice.ticket.query.entity.ProcessedEntry;
import ru.antonov.trainticketservice.ticket.query.entity.TicketView;
import ru.antonov.trainticketservice.ticket.query.repository.OccupiedSeatViewRepository;
import ru.antonov.trainticketservice.ticket.query.repository.ProcessedEntryRepository;
import ru.antonov.trainticketservice.ticket.query.repository.TicketViewRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketProjectorService {
    private final TicketViewRepository ticketViewRepository;
    private final OccupiedSeatViewRepository occupiedSeatViewRepository;
    private final ProcessedEntryRepository processedEntryRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(UUID entryId, String topic, String payload) {
        if (processedEntryRepository.existsById(entryId)) {
            log.info("Entry {} уже обработан. Пропускаем...", entryId);
            return;
        }

        try {
            switch (topic) {
                case "ticket-reserved-topic" -> {
                    TicketReservedEventData data = objectMapper.readValue(
                            payload, TicketReservedEventData.class
                    );
                    handleTicketReserved(data);
                }
                case "ticket-booked-topic" -> {
                    TicketBookedEventData data = objectMapper.readValue(
                            payload, TicketBookedEventData.class
                    );
                    handleTicketBooked(data);
                }
                case "ticket-cancelled-topic" -> {
                    TicketCancelledEventData data = objectMapper.readValue(
                            payload, TicketCancelledEventData.class
                    );
                    handleTicketCancelled(data);
                }
                case "ticket-cancel-pending-topic" -> {
                    TicketCancelPendingEventData data = objectMapper.readValue(
                            payload, TicketCancelPendingEventData.class
                    );
                    handleTicketCancelPending(data);
                }
                case "ticket-failed-to-cancel-topic" -> {
                    TicketFailedToCancelEventData data = objectMapper.readValue(
                            payload, TicketFailedToCancelEventData.class
                    );
                    handleTicketFailedToCancel(data);
                }
                case "ticket-failed-to-book-topic" -> {
                    TicketFailedToBookEventData data = objectMapper.readValue(
                            payload, TicketFailedToBookEventData.class
                    );
                    handleTicketFailedToBook(data);
                }
            }

            processedEntryRepository.save(
                    ProcessedEntry.builder()
                            .entryId(entryId)
                            .build()
            );
        } catch (JsonProcessingException ex) {
            log.error("FATAL: ошибка десериализация entry {}. Payload: {}", entryId, payload);
        }
    }

    private void handleTicketFailedToBook(TicketFailedToBookEventData data) {
        Optional<TicketView> entityOpt = ticketViewRepository.findById(data.getAggregateId());
        if (entityOpt.isEmpty()) {
            log.error("FATAL: нет view с ticketId {}. Пропуск события {}", data.getAggregateId(),
                    data);
            return;
        }
        TicketView entity = entityOpt.get();
        entity.setFailedAt(data.getFailedAt());
        entity.setLastUpdatedAt(data.getFailedAt());
        entity.setStatus(TicketView.Status.FAILED_TO_BOOK);
        ticketViewRepository.save(entity);

        occupiedSeatViewRepository.deleteBySeatIdAndCruiseId(data.getSeatId(), data.getCruiseId());
    }

    private void handleTicketFailedToCancel(TicketFailedToCancelEventData data) {
        Optional<TicketView> entityOpt = ticketViewRepository.findById(data.getAggregateId());
        if (entityOpt.isEmpty()) {
            log.error("FATAL: нет view с ticketId {}. Пропуск события {}", data.getAggregateId(),
                    data);
            return;
        }
        TicketView entity = entityOpt.get();
        entity.setFailedAt(data.getFailedAt());
        entity.setLastUpdatedAt(data.getFailedAt());
        entity.setStatus(TicketView.Status.FAILED_TO_CANCEL);
        ticketViewRepository.save(entity);
    }

    private void handleTicketCancelPending(TicketCancelPendingEventData data) {
        Optional<TicketView> entityOpt = ticketViewRepository.findById(data.getAggregateId());
        if (entityOpt.isEmpty()) {
            log.error("FATAL: нет view с ticketId {}. Пропуск события {}", data.getAggregateId(),
                    data);
            return;
        }
        TicketView entity = entityOpt.get();
        entity.setStatus(TicketView.Status.CANCEL_PENDING);
        ticketViewRepository.save(entity);
    }

    public void handleTicketReserved(TicketReservedEventData data) {
        TicketView ticketView = TicketView.builder()
                .ticketId(data.getAggregateId())
                .arrivalTime(data.getArrivalTime())
                .departureTime(data.getDepartureTime())
                .carriageCategory(data.getCarriageCategory())
                .carriageId(data.getCarriageId())
                .carriageNumber(data.getCarriageNumber())
                .seatId(data.getSeatId())
                .seatNumber(data.getSeatNumber())
                .seatCategory(data.getSeatCategory())
                .cruiseId(data.getCruiseId())
                .cruiseNumber(data.getCruiseNumber())
                .fare(data.getFare())
                .fromStopId(data.getFromStopId())
                .fromLocation(data.getFromLocation())
                .fromStation(data.getFromStation())
                .toStopId(data.getToStopId())
                .toStation(data.getToStation())
                .toLocation(data.getToLocation())
                .ownerId(data.getOwnerId())
                .ownerEmail(data.getOwnerEmail())
                .ownerSurname(data.getOwnerSurname())
                .ownerName(data.getOwnerName())
                .ownerPatronymic(data.getOwnerPatronymic())
                .status(TicketView.Status.RESERVED)
                .lastUpdatedAt(data.getReservedAt())
                .build();

        ticketViewRepository.save(ticketView);

        OccupiedSeatView seatView = OccupiedSeatView.builder()
                .seatId(data.getSeatId())
                .seatNumber(data.getSeatNumber())
                .cruiseId(data.getCruiseId())
                .cruiseNumber(data.getCruiseNumber())
                .owner(data.getOwnerSurname() + " " + data.getOwnerName() + " " + data.getOwnerPatronymic())
                .fromId(data.getFromStopId())
                .fromLocation(data.getFromLocation())
                .toId(data.getToStopId())
                .toLocation(data.getToLocation())
                .ticketId(data.getAggregateId())
                .build();

        occupiedSeatViewRepository.save(seatView);
    }

    public void handleTicketBooked(TicketBookedEventData data) {
        Optional<TicketView> entityOpt = ticketViewRepository.findById(data.getAggregateId());
        if (entityOpt.isEmpty()) {
            log.error("FATAL: нет view с ticketId {}. Пропуск события {}", data.getAggregateId(),
                    data);
            return;
        }
        TicketView entity = entityOpt.get();
        entity.setBookedAt(data.getBookedAt());
        entity.setStatus(TicketView.Status.BOOKED);
        ticketViewRepository.save(entity);
    }

    public void handleTicketCancelled(TicketCancelledEventData data) {
        Optional<TicketView> entityOpt = ticketViewRepository.findById(data.getAggregateId());
        if (entityOpt.isEmpty()) {
            log.error("FATAL: нет view с ticketId {}. Пропуск события {}", data.getAggregateId(),
                    data);
            return;
        }
        TicketView entity = entityOpt.get();
        entity.setCancelledAt(data.getCancelledAt());
        entity.setStatus(TicketView.Status.CANCELLED);
        ticketViewRepository.save(entity);

        occupiedSeatViewRepository.deleteBySeatIdAndCruiseId(data.getSeatId(), data.getCruiseId());
    }
}
