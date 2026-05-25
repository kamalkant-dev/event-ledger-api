package com.ledger.api.model;

import com.ledger.api.dto.EventResponse;

public record EventResult(EventResponse response, boolean duplicate) {}