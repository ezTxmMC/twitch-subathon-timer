package de.syntaxjason.repository;

import de.syntaxjason.model.EventType;
import de.syntaxjason.model.TimerEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventRepository {
    void save(TimerEvent event);
    List<TimerEvent> findAll();
    List<TimerEvent> findByUsername(String username);
    boolean existsByUsernameAndTypeAfter(String username, EventType eventType, LocalDateTime after);
    void deleteAll();
}
