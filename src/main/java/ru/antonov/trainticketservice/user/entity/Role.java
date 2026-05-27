package ru.antonov.trainticketservice.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum Role {
    USER,
    ADMIN;

    public Set<SimpleGrantedAuthority> getAuthorities() {
        return new HashSet<>(
                Set.of(
                        new SimpleGrantedAuthority("ROLE_" + this.name())
                )
        );
    }
}
