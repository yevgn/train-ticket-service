package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;

@Component
@RequiredArgsConstructor
public class EventDataMapper {

    private final ObjectMapper objectMapper;

    public EventData toEventData(Event event) {
        return switch (event.getEventType()) {

            case TICKET_BOOKED ->
                    objectMapper.convertValue(
                            event.getEventData(),
                            TicketBookedEventData.class
                    );

            case TICKET_RESERVED ->
                    objectMapper.convertValue(
                            event.getEventData(),
                            TicketReservedEventData.class
                    );
            case TICKET_FAILED_TO_BOOK ->
                objectMapper.convertValue(
                        event.getEventData(),
                        TicketFailedToBookEventData.class
                );
            case TICKET_CANCEL_PENDING ->
                objectMapper.convertValue(
                        event.getEventData(),
                        TicketCancelPendingEventData.class
                );
            case TICKET_CANCELLED ->
                objectMapper.convertValue(
                        event.getEventData(),
                        TicketCancelledEventData.class
                );
            case TICKET_FAILED_TO_CANCEL ->
                objectMapper.convertValue(
                        event.getEventData(),
                        TicketFailedToCancelEventData.class
                );
        };
    }
}
