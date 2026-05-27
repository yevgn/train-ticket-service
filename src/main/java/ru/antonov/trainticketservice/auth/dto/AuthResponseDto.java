package ru.antonov.trainticketservice.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.trainticketservice.user.entity.Role;

import java.util.UUID;

@Builder
@Getter
@Setter
public class AuthResponseDto {
    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;

    private Role role;
}

