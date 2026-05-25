package com.ledger.api.controller;

import com.ledger.api.dto.*;
import com.ledger.api.model.EventResult;
import com.ledger.api.service.EventService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Tag(name = "Event Ledger", description = "Financial transaction event management")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    @Operation(summary = "Submit a transaction event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created or duplicate event returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("POST /events called for eventId={}", request.getEventId());
        EventResult result = eventService.createEvent(request);
        return ResponseEntity
                .status(result.duplicate() ? HttpStatus.OK : HttpStatus.CREATED)
                .body(result.response());
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Retrieve a single event by ID")
    public ResponseEntity<EventResponse> getEvent(@PathVariable("id") String id) {
        log.info("GET /events/{}", id);
        return ResponseEntity.ok(eventService.getEventById(id));
    }

@GetMapping("/events")
@Operation(summary = "Get events by account", description = "Returns events sorted by eventTimestamp with pagination support")
public ResponseEntity<List<EventResponse>> getEvents(
        @RequestParam String account,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    return ResponseEntity.ok(
            eventService.getEventsByAccount(account, page, size)
    );
}

    @GetMapping("/accounts/{accountId}/balance")
    @Operation(summary = "Get the current computed balance for an account")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("accountId") String accountId) {
        log.info("GET /accounts/{}/balance", accountId);
        return ResponseEntity.ok(eventService.getBalance(accountId));
    }
}