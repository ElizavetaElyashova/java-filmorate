package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private long id;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotBlank
    private long entityId;
    @NotBlank
    private long userId;
    private Long timestamp;
}
