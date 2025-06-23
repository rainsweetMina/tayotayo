package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.files ORDER BY n.createdDate DESC")
    List<Notice> findAllByOrderByCreatedDateDesc();

    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.files WHERE n.id = :id")
    Optional<Notice> findByIdWithFiles(Long id);

    Optional<Notice> findFirstByShowPopupTrueAndPopupStartBeforeAndPopupEndAfterOrderByPopupStartDesc(
            LocalDateTime now1, LocalDateTime now2);

    long countByCreatedDateBetween(Timestamp start, Timestamp end);

}
