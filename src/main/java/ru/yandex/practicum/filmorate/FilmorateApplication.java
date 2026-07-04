package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FilmorateApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(FilmorateApplication.class, args);
        } catch (Exception e) {
            //отлов и вывод в лог ошибок автоматической валидации аннотациями
            log.warn(e.getMessage());
        }
    }

}
