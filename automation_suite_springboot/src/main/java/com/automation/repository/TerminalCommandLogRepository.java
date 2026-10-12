package com.automation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automation.domain.TerminalCommandLog;

@Repository
public interface TerminalCommandLogRepository extends JpaRepository<TerminalCommandLog, String> {

    List<TerminalCommandLog> findByTerminalIdOrderByTimestampAsc(String terminalId);

    java.util.Optional<TerminalCommandLog> findTopByTerminalIdOrderByTimestampDesc(String terminalId);

    @Modifying
    @Query("UPDATE TerminalCommandLog c SET c.result = NULL WHERE c.timestamp < :cutoff AND c.result IS NOT NULL")
    int nullifyResultsOlderThan(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalCommandLog c WHERE c.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalCommandLog c WHERE c.terminalId = :terminalId")
    void deleteByTerminalId(@Param("terminalId") String terminalId);
}
