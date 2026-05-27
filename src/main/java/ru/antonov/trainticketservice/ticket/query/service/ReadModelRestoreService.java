package ru.antonov.trainticketservice.ticket.query.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;
import ru.antonov.trainticketservice.ticket.query.projector.TicketProjectorService;
import ru.antonov.trainticketservice.ticket.query.repository.OccupiedSeatViewRepository;
import ru.antonov.trainticketservice.ticket.query.repository.TicketViewRepository;

import java.util.List;

/**
 * Восстанавливает read model путем повторного проигрывания событий из Event Store.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadModelRestoreService {
    private final EventRepository eventRepository;
    private final TicketProjectorService projectorService;
    private final TicketViewRepository ticketViewRepository;
    private final OccupiedSeatViewRepository occupiedSeatViewRepository;

    /**
     * Удаляет текущие views и проигрывает сохраненные события по порядку времени.
     */
    @Transactional
    public void restore() {
        ticketViewRepository.deleteAll();
        occupiedSeatViewRepository.deleteAll();

        List<Event> events = eventRepository.findAllOrderByTimestamp();

        for (Event event : events) {
            try {
                projectorService.handle(
                        event.getId(),
                        mapEventTypeToTopic(event.getEventType()),
                        event.getPayload()
                );
            } catch (Exception ex) {
                log.error("Ошибка при обработке события {} типа {}: {}",
                        event.getId(), event.getEventType(), ex.getMessage());
            }
        }
    }

    private String mapEventTypeToTopic(Event.EventType eventType) {
        return switch (eventType) {
            case TICKET_RESERVED       -> "ticket-reserved-topic";
            case TICKET_BOOKED         -> "ticket-booked-topic";
            case TICKET_CANCELLED      -> "ticket-cancelled-topic";
            case TICKET_CANCEL_PENDING -> "ticket-cancel-pending-topic";
            case TICKET_FAILED_TO_BOOK -> "ticket-failed-to-book-topic";
            case TICKET_FAILED_TO_CANCEL -> "ticket-failed-to-cancel-topic";
        };
    }
}
