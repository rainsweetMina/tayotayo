package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    Page<AdminAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

}
