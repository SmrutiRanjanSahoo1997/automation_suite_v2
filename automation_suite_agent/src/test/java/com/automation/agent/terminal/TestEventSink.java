package com.automation.agent.terminal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

/** In-memory event sink for manager tests; serializes to maps for assertions. */
final class TestEventSink implements Consumer<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Object> events = new CopyOnWriteArrayList<>();

    @Override
    public void accept(Object eventPojo) {
        events.add(eventPojo);
    }

    List<Object> events() {
        return events;
    }

    /** Raw pojos of the given runtime type, in arrival order. */
    @SuppressWarnings("unchecked")
    <T> List<T> ofType(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(e -> (T) e)
                .toList();
    }

    /** All events serialized to maps (covers Map-typed events like terminal_max). */
    List<java.util.Map<String, Object>> asMaps() {
        return events.stream()
                .map(e -> MAPPER.convertValue(e, java.util.Map.class))
                .map(m -> (java.util.Map<String, Object>) m)
                .toList();
    }

    List<java.util.Map<String, Object>> withWireType(String wireType) {
        return asMaps().stream()
                .filter(m -> wireType.equals(String.valueOf(m.get("event-type"))))
                .toList();
    }

    void clear() {
        events.clear();
    }
}