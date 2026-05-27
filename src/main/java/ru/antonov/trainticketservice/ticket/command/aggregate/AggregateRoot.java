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

/**
 * Базовый класс для агрегатов, построенных по паттерну Event Sourcing.
 * <p>
 * Хранит идентификатор агрегата, текущую версию, загруженную историю и события,
 * созданные текущей командой, но еще не сохраненные в Event Store.
 */
@SuperBuilder
public abstract class AggregateRoot {
    @Getter
    private final UUID id;

    @Getter
    private long version = 0;

    private final List<Event> uncommittedEvents = new ArrayList<>();

    @Getter
    private List<Event> history = new ArrayList<>();

    /**
     * Создает агрегат с постоянным идентификатором.
     *
     * @param id идентификатор агрегата
     */
    public AggregateRoot(UUID id){
        this.id = id;
    }

    /**
     * Возвращает события, созданные после восстановления агрегата из истории.
     *
     * @return неизменяемый список несохраненных событий
     */
    public List<Event> getUncommitedEvents(){
        return Collections.unmodifiableList(uncommittedEvents);
    }

    /**
     * Применяет новое доменное событие и регистрирует его для сохранения.
     *
     * @param event метаданные события и модель для сохранения
     * @param data payload
     */
    protected void applyEvent(Event event, EventData data){
        handleEvent(event, data);
        event.setAggregateVersion(++version);
        uncommittedEvents.add(event);
    }

    /**
     * Изменяет состояние агрегата на основе события.
     *
     * @param event метаданные сохраненного события
     * @param data payload
     */
    protected abstract void handleEvent(Event event, EventData data);

    /**
     * Очищает список несохраненных событий после успешного сохранения.
     */
    public void markEventsAsCommitted(){
        uncommittedEvents.clear();
    }

    /**
     * Восстанавливает состояние агрегата из упорядоченной истории событий.
     *
     * @param history история событий агрегата
     * @param mapper mapper, восстанавливающий типизированные данные из событий
     */
    public void loadFromHistory(List<Event> history, EventDataMapper mapper) {
        this.history = new ArrayList<>(history);
        for (Event event : this.history) {
            EventData data = mapper.toEventData(event);
            handleEvent(event, data);
            this.version = event.getAggregateVersion();
        }
    }
}
