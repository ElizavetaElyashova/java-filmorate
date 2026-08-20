package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Operation {
    private int id;
    @NotBlank
    private String name;

    public Operation(int id) {
        this.id = id;
    }

    public Operation() {}
}
