package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedDateDesc();

    Optional<Notice> findFirstByShowPopupTrueAndPopupStartBeforeAndPopupEndAfterOrderByPopupStartDesc(
            LocalDateTime now1, LocalDateTime now2);



}
