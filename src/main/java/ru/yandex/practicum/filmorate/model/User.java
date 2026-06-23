package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.NotContainsSpaces;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id", "email"})
@Builder
public class User {
    private long id;
    @Email
    private String email;
    @NotBlank
    @NotEmpty
    @NotContainsSpaces
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
