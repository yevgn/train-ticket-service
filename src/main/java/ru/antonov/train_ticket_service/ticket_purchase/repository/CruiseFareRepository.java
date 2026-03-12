package ru.antonov.train_ticket_service.ticket_purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.antonov.train_ticket_service.ticket_purchase.entity.CruiseFare;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CruiseFareRepository extends JpaRepository<CruiseFare, UUID> {
    @Query("""
            SELECT cf FROM CruiseFare cf
            
            JOIN FETCH cf.cruise c
            JOIN FETCH cf.seatCategory sc
            JOIN FETCH cf.carriageCategory cc
            
            WHERE c.id = :cruiseId
            AND sc.id = :seatCategoryId
            AND cc.id = :carriageCategoryId
            """)
    Optional<CruiseFare> findByParamsFetchAllFields(UUID cruiseId, UUID seatCategoryId, UUID carriageCategoryId);
}
