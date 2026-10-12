package com.automation.mssql.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.automation.mssql.entity.MssqlQueryAuditLog;

@Repository
public interface MssqlQueryAuditLogRepository extends JpaRepository<MssqlQueryAuditLog, Long> {

    List<MssqlQueryAuditLog> findByUserIdOrderByExecutedAtDesc(String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MssqlQueryAuditLog a WHERE a.executedAt < :cutoff")
    void deleteByExecutedAtBefore(long cutoff);
}
