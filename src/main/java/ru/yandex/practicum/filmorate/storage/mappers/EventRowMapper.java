package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .id(rs.getLong("event_id"))
                .entityId(rs.getLong("entity_id"))
                .userId(rs.getLong("user_id"))
                .timestamp(rs.getObject("timestamp", Instant.class).toEpochMilli())
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation"))
                .build();
    }
}
