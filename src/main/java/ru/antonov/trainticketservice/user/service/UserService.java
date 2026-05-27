package ru.antonov.trainticketservice.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.antonov.trainticketservice.auth.service.JwtService;
import ru.antonov.trainticketservice.auth.service.TokenService;
import ru.antonov.trainticketservice.common.exception.DbConstraintViolationException;
import ru.antonov.trainticketservice.common.exception.EntityNotFoundException;
import ru.antonov.trainticketservice.common.exception.ErrorCode;
import ru.antonov.trainticketservice.common.exception.ResourceAccessDeniedException;
import ru.antonov.trainticketservice.user.dto.DtoFactory;
import ru.antonov.trainticketservice.user.dto.UserRegisterRequestDto;
import ru.antonov.trainticketservice.user.dto.UserResponseDto;
import ru.antonov.trainticketservice.user.entity.Role;
import ru.antonov.trainticketservice.user.entity.User;
import ru.antonov.trainticketservice.user.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;

    public User save(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            Throwable root = NestedExceptionUtils.getRootCause(ex);
            String message;
            String debugMessage;

            if (root instanceof SQLException sqlEx && sqlEx.getMessage().toLowerCase().contains("unique") &&
                    sqlEx.getMessage().toLowerCase().contains("email")) {
                message = String.format("Ошибка. Пользователь с email %s уже существует", user.getEmail());
                debugMessage = String.format("Ошибка при добавлении пользователя. Пользователь с email %s уже " +
                        "существует", user.getEmail());
            } else {
                message = "Нарушены ограничения целостности данных. Возможно, вы пытаетесь добавить некорректные или" +
                        "уже существующие данные";
                debugMessage = (root != null ? root.getMessage() : ex.getMessage());
            }

            throw new DbConstraintViolationException(message, debugMessage, ErrorCode.DB_CONSTRAINT_VIOLATION);
        }
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserResponseDto registerUser(UserRegisterRequestDto request) {
        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .patronymic(request.getPatronymic())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .email(request.getEmail())
                .build();
        save(user);

        return DtoFactory.makeUserResponseDto(user);
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public UserResponseDto findUserById(User principal, UUID userId) {
        User other = findById(userId).orElseThrow(() -> new EntityNotFoundException(
                "Пользователь не найден",
                String.format("Ошибка получения информации пользователя %s пользователем %s: пользователь не найден",
                        userId, principal.getId()),
                ErrorCode.ENTITY_NOT_FOUND
        ));

        checkPrincipalHasAccessToReadOtherInfoElseThrow(principal, other);

        return DtoFactory.makeUserResponseDto(other);
    }

    private void checkPrincipalHasAccessToReadOtherInfoElseThrow(User principal, User other) {
        if (principal.getRole() == Role.ADMIN) return;

        else if (!principal.equals(other)) {
            throw new ResourceAccessDeniedException(
                    "Нет доступа",
                    String.format("Ошибка получения информации пользователя %s пользователем %s: нет доступа",
                            other.getId(), principal.getId()),
                    ErrorCode.FORBIDDEN
            );
        }
    }
}