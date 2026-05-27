package ru.antonov.trainticketservice.user.dto;

import ru.antonov.trainticketservice.user.entity.User;

public class DtoFactory {
    public static UserResponseDto makeUserResponseDto(User user){
        return UserResponseDto
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .surname(user.getSurname())
                .name(user.getName())
                .patronymic(user.getPatronymic())
                .role(user.getRole())
                .build();
    }
}
