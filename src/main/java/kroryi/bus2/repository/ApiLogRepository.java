package kroryi.bus2.repository;

import kroryi.bus2.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    List<ApiLog> findTop10BySuccessIsFalseOrderByTimestampDesc();  // 최근 실패 로그 10개를 시간 역순으로 정렬
    List<ApiLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end); // 시간대별 로그 조회. API 성공률/실패률 그래프에 사용할 데이터
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end); // 오늘 하루 동안 요청된 API 로그의 총 개수를 셈

}
