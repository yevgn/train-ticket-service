package ru.antonov.train_ticket_service.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.train_ticket_service.user.entity.Role;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private UUID id;

    private String name;

    private String surname;

    private String patronymic;

    private String email;

    private Role role;
}
