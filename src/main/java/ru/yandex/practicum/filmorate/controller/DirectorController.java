package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Director> createDirector(@RequestBody Director director) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(filmService.createDirector(director.getName()));
    }

    @GetMapping
    public ResponseEntity<Collection<Director>> findAllDirectors() {
        return ResponseEntity.ok(filmService.findAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> findDirectorById(@PathVariable Long id) {
        return ResponseEntity.ok(filmService.findDirectorById(id));
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@RequestBody Director updatedDirector) {
        return ResponseEntity.ok(filmService.updateDirector(updatedDirector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDirector(@PathVariable Long id) {
        filmService.deleteDirector(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
