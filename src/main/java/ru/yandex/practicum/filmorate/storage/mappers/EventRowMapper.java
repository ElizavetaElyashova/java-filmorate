package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = Event.builder()
                .id(rs.getLong("event_id"))
                .entityId(rs.getLong("entity_id"))
                .userId(rs.getLong("user_id"))
                .timestamp(rs.getObject("timestamp", LocalDateTime.class))
                .build();
        EventType eventType = new EventType();
        eventType.setId(rs.getInt("event_type_id"));
        eventType.setName(rs.getString("event_type"));
        Operation operation = new Operation();
        operation.setId(rs.getInt("operation_id"));
        operation.setName(rs.getString("operation"));
        event.setEventType(eventType);
        event.setOperation(operation);
        return event;
    }
}
