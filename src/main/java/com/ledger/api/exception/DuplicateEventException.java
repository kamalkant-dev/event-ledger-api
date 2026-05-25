package com.ledger.api.exception;

public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String eventId) {
        super("Event already exists with id: " + eventId);
    }
}
