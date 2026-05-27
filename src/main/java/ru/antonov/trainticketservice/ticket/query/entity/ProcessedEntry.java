package ru.antonov.trainticketservice.ticket.query.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "processed_entries")
public class ProcessedEntry {
    @Id
    private UUID entryId;
}
