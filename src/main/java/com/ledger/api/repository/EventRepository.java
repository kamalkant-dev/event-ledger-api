package com.ledger.api.repository;

import com.ledger.api.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    Optional<Event> findTopByAccountIdOrderByEventTimestampDesc(String accountId);
    Page<Event> findByAccountIdOrderByEventTimestampAsc(String accountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Event e WHERE e.accountId = :accountId AND e.type = 'CREDIT'")
    BigDecimal sumCreditsByAccountId(@Param("accountId") String accountId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Event e WHERE e.accountId = :accountId AND e.type = 'DEBIT'")
    BigDecimal sumDebitsByAccountId(@Param("accountId") String accountId);
}
