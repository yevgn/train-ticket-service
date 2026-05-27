package ru.antonov.trainticketservice.ticket.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.ticket.query.entity.ProcessedEntry;

import java.util.UUID;

@Repository
public interface ProcessedEntryRepository extends JpaRepository<ProcessedEntry, UUID> {
}
