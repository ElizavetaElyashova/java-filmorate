package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.NotContainsSpaces;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
public class User {
    private long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @NotContainsSpaces
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
    private Set<Long> friendsIds;
}
