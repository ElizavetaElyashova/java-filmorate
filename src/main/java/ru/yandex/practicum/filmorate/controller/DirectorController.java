package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    public ResponseEntity<Director> createDirector(@RequestBody @Valid Director director) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directorService.createDirector(director.getName()));
    }

    @GetMapping
    public ResponseEntity<Collection<Director>> findAllDirectors() {
        return ResponseEntity.ok(directorService.findAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> findDirectorById(@PathVariable Long id) {
        return ResponseEntity.ok(directorService.findDirectorById(id));
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@RequestBody Director updatedDirector) {
        return ResponseEntity.ok(directorService.updateDirector(updatedDirector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDirector(@PathVariable Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
