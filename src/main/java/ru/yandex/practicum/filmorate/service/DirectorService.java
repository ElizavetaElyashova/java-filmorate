package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;

    public Director createDirector(String name) {
        Set<String> directorsNames = directorStorage.findAllDirectors().stream()
                .map(director -> director.getName().toLowerCase())
                .collect(Collectors.toSet());
        if (directorsNames.contains(name.toLowerCase()))
            throw new DuplicateException("Режиссер с таким именем уже содержится в БД");
        return directorStorage.createDirector(name);
    }

    public List<Director> findAllDirectors() {
        return directorStorage.findAllDirectors();
    }

    public Director findDirectorById(Long id) {
        return directorStorage.findDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Не удалось найти режиссера с id = " + id.toString()));
    }

    public Director updateDirector(Director updatedDirector) {
        if (!isDirectorExist(updatedDirector.getId())) {
            throw new NotFoundException("Отсутствует режиссер с id = " + updatedDirector.getId().toString());
        }

        return directorStorage.updateDirector(updatedDirector);
    }

    public void deleteDirector(Long id) {
        if (!isDirectorExist(id)) {
            throw new NotFoundException("Отсутствует режиссер с id = " + id.toString());
        }

        directorStorage.deleteDirector(id);
    }

    private boolean isDirectorExist(Long id) {
        return directorStorage.findAllDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet()).contains(id);
    }
}
