package ru.antonov.train_ticket_service.ticket_purchase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.antonov.train_ticket_service.ticket_purchase.entity.CruiseFare;
import ru.antonov.train_ticket_service.ticket_purchase.repository.CruiseFareRepository;

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
