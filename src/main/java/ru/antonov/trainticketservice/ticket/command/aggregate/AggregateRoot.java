package ru.antonov.trainticketservice.ticket.command.aggregate;

import lombok.Getter;

import lombok.experimental.SuperBuilder;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventData;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;

import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuperBuilder
public abstract class AggregateRoot {
    @Getter
    private final UUID id;

    @Getter
    private long version = 0;

    private final List<Event> uncommittedEvents = new ArrayList<>();

    @Getter
    private List<Event> history = new ArrayList<>();

    public AggregateRoot(UUID id){
        this.id = id;
    }

    public List<Event> getUncommitedEvents(){
        return Collections.unmodifiableList(uncommittedEvents);
    }

    protected void applyEvent(Event event, EventData data){
        handleEvent(event, data);
        event.setAggregateVersion(++version);
        uncommittedEvents.add(event);
    }

    protected abstract void handleEvent(Event event, EventData data);

    public void markEventsAsCommitted(){
        uncommittedEvents.clear();
    }

    public void loadFromHistory(List<Event> history, EventDataMapper mapper) {
        this.history = new ArrayList<>(history);
        for (Event event : this.history) {
            EventData data = mapper.toEventData(event);
            handleEvent(event, data);
            this.version = event.getAggregateVersion();
        }
    }
}
