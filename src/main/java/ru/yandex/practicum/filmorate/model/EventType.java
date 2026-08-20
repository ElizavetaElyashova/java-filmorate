package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventType {
    private int id;
    @NotBlank
    private String name;

    public EventType(int id) {
        this.id = id;
    }

    public EventType() {}
}
