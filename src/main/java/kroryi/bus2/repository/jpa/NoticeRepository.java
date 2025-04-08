package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedDateDesc();
}
