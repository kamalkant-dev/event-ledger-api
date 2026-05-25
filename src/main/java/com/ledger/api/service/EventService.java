package com.ledger.api.service;

import com.ledger.api.dto.*;
import com.ledger.api.exception.EventNotFoundException;
import com.ledger.api.model.Event;
import com.ledger.api.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {

        Object lock = locks.computeIfAbsent(request.getEventId(), k -> new Object());

        synchronized (lock) {
            try {
                log.info("Processing eventId={}", request.getEventId());
                //return existing instead of throwing error
                return eventRepository.findById(request.getEventId())
                        .map(existing -> {
                            log.warn("Duplicate event ignored: {}", request.getEventId());
                            return toResponse(existing);
                        })
                        .orElseGet(() -> {
                            Event event = Event.builder()
                                    .eventId(request.getEventId())
                                    .accountId(request.getAccountId())
                                    .type(request.getType())
                                    .amount(request.getAmount())
                                    .currency(request.getCurrency())
                                    .eventTimestamp(request.getEventTimestamp())
                                    .metadata(request.getMetadata())
                                    .receivedAt(Instant.now())
                                    .build();

                            Event saved = eventRepository.save(event);
                            log.info("Event saved successfully: {}", saved.getEventId());
                            return toResponse(saved);
                        });

            } finally {
                locks.remove(request.getEventId());
            }
        }
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(String eventId) {
        log.info("Fetching event by id={}", eventId);
        return eventRepository.findById(eventId)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByAccount(String accountId) {
        log.info("Fetching events for account={}", accountId);

        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountId) {
        log.info("Calculating balance for account={}", accountId);

        BigDecimal credits = eventRepository.sumCreditsByAccountId(accountId);
        BigDecimal debits = eventRepository.sumDebitsByAccountId(accountId);

        credits = credits == null ? BigDecimal.ZERO : credits;
        debits = debits == null ? BigDecimal.ZERO : debits;

        BigDecimal balance = credits.subtract(debits);
        List<Event> events =
                eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId);
        String currency = events.isEmpty()
                ? "USD"
                : events.get(events.size() - 1).getCurrency();

        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .currency(currency)
                .build();
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .accountId(event.getAccountId())
                .type(event.getType())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .eventTimestamp(event.getEventTimestamp())
                .metadata(event.getMetadata())
                .receivedAt(event.getReceivedAt())
                .build();
    }
}