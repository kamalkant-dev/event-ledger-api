package com.ledger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.api.dto.EventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EventLedgerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EventRequest buildEvent(String eventId, String accountId, String type,
                                    double amount, String timestamp) {

        EventRequest req = new EventRequest();
        req.setEventId(eventId);
        req.setAccountId(accountId);
        req.setType(type);
        req.setAmount(BigDecimal.valueOf(amount));
        req.setCurrency("USD");
        req.setEventTimestamp(Instant.parse(timestamp));
        req.setMetadata(Map.of("source", "test"));
        return req;
    }

    // ───────────── POST /events ─────────────

    @Test
    @DisplayName("POST /events - create event successfully")
    void createEvent_success() throws Exception {

        EventRequest req = buildEvent(
                "evt-001",
                "acct-123",
                "CREDIT",
                150.00,
                "2026-05-15T14:02:11Z"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("evt-001"))
                .andExpect(jsonPath("$.type").value("CREDIT"));
    }

    @Test
    @DisplayName("POST /events - duplicate event returns same event (idempotency)")
    void createEvent_duplicate_returnsSameEvent() throws Exception {

        EventRequest req = buildEvent(
                "evt-dup",
                "acct-123",
                "CREDIT",
                200.00,
                "2026-05-15T10:00:00Z"
        );

        // first call
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // duplicate call → should NOT crash, should still return valid event
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-dup"));
    }

    // ───────────── GET EVENTS ─────────────

    @Test
    @DisplayName("GET /events?account - returns ordered events")
    void getEvents_ordered() throws Exception {

        String account = "acct-order";

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildEvent("evt-2", account, "CREDIT", 100,
                                "2026-05-15T12:00:00Z"))));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildEvent("evt-1", account, "CREDIT", 50,
                                "2026-05-15T08:00:00Z"))));

        mockMvc.perform(get("/events")
                        .param("account", account))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventId").value("evt-1"))
                .andExpect(jsonPath("$[1].eventId").value("evt-2"));
    }

    // ───────────── BALANCE ─────────────

    @Test
    @DisplayName("GET /balance - correct calculation")
    void balance_calculation() throws Exception {

        String account = "acct-bal";

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildEvent("c1", account, "CREDIT", 500,
                                "2026-05-01T10:00:00Z"))));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildEvent("c2", account, "CREDIT", 300,
                                "2026-05-02T10:00:00Z"))));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildEvent("d1", account, "DEBIT", 200,
                                "2026-05-03T10:00:00Z"))));

        mockMvc.perform(get("/accounts/" + account + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account))
                .andExpect(jsonPath("$.balance").value(600.00));
    }

    // ───────────── VALIDATION ─────────────

    @Test
    @DisplayName("POST /events - invalid amount rejected")
    void validation_negativeAmount() throws Exception {

        EventRequest req = buildEvent(
                "bad",
                "acct-1",
                "CREDIT",
                0,
                "2026-05-15T10:00:00Z"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ───────────── GET BY ID ─────────────

    @Test
    @DisplayName("GET /events/{id} - success")
    void getById_success() throws Exception {

        EventRequest req = buildEvent(
                "evt-get",
                "acct-1",
                "DEBIT",
                75,
                "2026-05-15T09:00:00Z"
        );

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(get("/events/evt-get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-get"));
    }
}