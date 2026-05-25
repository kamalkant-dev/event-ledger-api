package com.ledger.api.repository;

import com.ledger.api.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Event e WHERE e.accountId = :accountId AND e.type = 'CREDIT'")
    BigDecimal sumCreditsByAccountId(@Param("accountId") String accountId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Event e WHERE e.accountId = :accountId AND e.type = 'DEBIT'")
    BigDecimal sumDebitsByAccountId(@Param("accountId") String accountId);
}
