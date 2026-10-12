package com.automation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automation.domain.TerminalSessionLog;

@Repository
public interface TerminalSessionRepository extends JpaRepository<TerminalSessionLog, String> {

    Optional<TerminalSessionLog> findByTerminalId(String terminalId);

    List<TerminalSessionLog> findByUserId(String userId);

    List<TerminalSessionLog> findByHost(String host);

    @Query("SELECT t FROM TerminalSessionLog t WHERE t.state = 'CLOSED' AND t.lastActivityAt < :cutoff")
    List<TerminalSessionLog> findExpiredClosedSessions(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalSessionLog t WHERE t.id IN :ids")
    void deleteAllByIds(@Param("ids") List<String> ids);
}
