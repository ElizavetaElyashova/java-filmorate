package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Getter
@Setter
public class Mpa {
    private int id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Mpa mpa = (Mpa) o;
        return id == mpa.id && Objects.equals(name, mpa.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
