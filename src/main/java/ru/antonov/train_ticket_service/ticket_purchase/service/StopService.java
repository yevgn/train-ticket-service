package ru.antonov.train_ticket_service.ticket_purchase.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;
import ru.antonov.train_ticket_service.ticket_purchase.repository.StopRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StopService {
    private final StopRepository stopRepository;

    public Optional<Stop> findById(UUID stopId){
        return stopRepository.findById(stopId);
    }

    public Stop save(Stop stop) throws OptimisticLockException {
        return stopRepository.save(stop);
    }

    public List<Stop> findAllStopsByCruiseIdSortByPlannedArrival(UUID cruiseId){
        return stopRepository.findAllByCruiseIdSortByPlannedArrival(cruiseId);
    }

    public Optional<Stop> findByCruiseIdAndStationIdWithStation(UUID cruiseId, UUID stationId){
        return stopRepository.findByCruiseIdAndStationIdWithStation(cruiseId, stationId);
    }

    public boolean isValidRoute(String cruiseId, UUID fromStationId, UUID toStationId){
        return stopRepository.isValidRoute(cruiseId, fromStationId, toStationId);
    }

    public Optional<Stop> findByCruiseIdAndStopOrder(UUID cruiseId, Integer stopOrder){
        return stopRepository.findByCruiseIdAndStopOrder(cruiseId, stopOrder);
    }

    public Optional<Stop> findFirstStopByCruiseIdWithStation(UUID cruiseId){
        return stopRepository.findFirstStopByCruiseIdWithStation(cruiseId);
    }

    public Optional<Stop> findFirstStopByCruiseId(UUID cruiseId){
        return stopRepository.findFirstStopByCruiseId(cruiseId);
    }

    public Optional<Stop> findLastStopByCruiseIdWithStation(UUID cruiseId){
        return stopRepository.findLastStopByCruiseIdWithStation(cruiseId);
    }

    public Optional<Stop> findLastStopByCruiseId(UUID cruiseId){
        return stopRepository.findLastStopByCruiseId(cruiseId);
    }

    public Optional<Stop> findByIdWithStation(UUID stopId){
        return stopRepository.findByIdWithStation(stopId);
    }
}
