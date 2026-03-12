package ru.antonov.kafka;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private UUID id;
    private String email;
    private String surname;
    private String name;
    private String patronymic;
}
