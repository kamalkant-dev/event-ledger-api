package com.ledger.api.controller;

import com.ledger.api.dto.*;
import com.ledger.api.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("POST /events called for eventId={}", request.getEventId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable("id") String id) {
        log.info("GET /events/{}", id);
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getEvents(@RequestParam("account") String account) {
        log.info("GET /events?account={}", account);
        return ResponseEntity.ok(eventService.getEventsByAccount(account));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("accountId") String accountId) {
        log.info("GET /accounts/{}/balance", accountId);
        return ResponseEntity.ok(eventService.getBalance(accountId));
    }
}