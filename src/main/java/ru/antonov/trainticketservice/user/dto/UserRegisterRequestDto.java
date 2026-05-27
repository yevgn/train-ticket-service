package ru.antonov.trainticketservice.user.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {
    @NotBlank(message = "поле surname не должно быть пустым")
    @Size(max = 30, message = "Длина surname не может превышать 30 символов")
    @Pattern(
            regexp = "^[а-яА-Я]{2,}$",
            message = "Неправильный формат фамилии"
    )
    private String surname;

    @NotBlank(message = "поле name не должно быть пустым")
    @Size(max = 30, message = "Длина name не может превышать 30 символов")
    @Pattern(
            regexp = "^[а-яА-Я]{2,}$",
            message = "Неправильный формат имени"
    )
    private String name;

    @Size(max = 30, message = "Длина patronymic не может превышать 30 символов")
    @Pattern(
            regexp = "^[а-яА-Я]{2,}$",
            message = "Неправильный формат отчества"
    )
    private String patronymic;

    @NotBlank(message = "Поле email не может быть пустым")
    @Size(max = 50, message = "Длина email не может превышать 50 символов")
    @Pattern(
            regexp = "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)?@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Неправильный формат email"
    )
    private String email;

    @NotBlank(message = "Поле password не может быть пустым")
    @Size(min = 8, max = 50, message = "Длина email не может быть меньше 8 символов и не может превышать 50 символов")
    private String password;

}