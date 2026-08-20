package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public class FeedDbStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EventRowMapper eventMapper;

    private String findFeedByUserIdQuery = "SELECT f.event_id, f.event_type_id, f.operation_id, f.entity_id, f.user_id, f.timestamp," +
            "e.name AS event_type, o.name AS operation " +
            "FROM feed f " +
            "JOIN event_types AS e ON f.event_type_id = e.id " +
            "JOIN operations AS o ON f.operation_id = o.id " +
            "WHERE f.user_id = ?";
    private String insertEventQuery = "INSERT INTO feed(event_type_id, operation_id, entity_id, user_id, timestamp) " +
            "VALUES(?, ?, ?, ?, ?)";

    public Collection<Event> findFeedByUserId(long userId) {
        List<Event> feed = jdbc.query(findFeedByUserIdQuery, eventMapper, userId);
        return feed;
    }

    public void create(Event event) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertEventQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, event.getEventType().getId());
            ps.setObject(2, event.getOperation().getId());
            ps.setObject(3, event.getEntityId());
            ps.setObject(4, event.getUserId());
            ps.setObject(5, LocalDateTime.now());
            return ps;
        }, keyHolder);
    }
}
