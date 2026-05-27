package ru.antonov.trainticketservice.cruise.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.antonov.trainticketservice.cruise.entity.CruiseFare;
import ru.antonov.trainticketservice.cruise.repository.CruiseFareRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CruiseFareService {
    private final CruiseFareRepository cruiseFareRepository;

    public Optional<CruiseFare> findByParamsFetchAllFields(UUID cruiseId, UUID seatCategoryId, UUID carriageCategoryId){
        return cruiseFareRepository.findByParamsFetchAllFields(cruiseId, seatCategoryId, carriageCategoryId);
    }
}
