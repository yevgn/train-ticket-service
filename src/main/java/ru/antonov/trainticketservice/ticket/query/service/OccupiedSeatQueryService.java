package ru.antonov.trainticketservice.ticket.query.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.trainticketservice.ticket.query.dto.DtoFactory;
import ru.antonov.trainticketservice.ticket.query.dto.OccupiedSeatDto;
import ru.antonov.trainticketservice.ticket.query.entity.OccupiedSeatView;
import ru.antonov.trainticketservice.ticket.query.repository.OccupiedSeatViewRepository;

import java.util.List;
import java.util.UUID;

/**
 * Query-side сервис для занятых мест, построенных на основе проекций билетов.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OccupiedSeatQueryService {
    private final OccupiedSeatViewRepository repository;

    /**
     * Возвращает занятые места для указанного рейса.
     *
     * @param cruiseId идентификатор рейса
     * @return DTO занятых мест
     */
    public List<OccupiedSeatDto> findOccupiedSeatsByCruiseId(UUID cruiseId){
        List<OccupiedSeatView> views = repository.findAllByCruiseId(cruiseId);

        return views.stream()
                .map(DtoFactory::makeOccupiedSeatDto)
                .toList();
    }
}
