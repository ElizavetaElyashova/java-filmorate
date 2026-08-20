package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Event {
    private long id;
    @NotBlank
    private EventType eventType;
    @NotBlank
    private Operation operation;
    @NotBlank
    private long entityId;
    @NotBlank
    private long userId;
    @PastOrPresent
    private LocalDateTime timestamp;
}
