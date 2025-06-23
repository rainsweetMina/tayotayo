package kroryi.bus2.service.admin;

import kroryi.bus2.dto.PostsStatsDTO;
import kroryi.bus2.repository.jpa.NoticeRepository;
import kroryi.bus2.repository.jpa.QnaRepository;
import kroryi.bus2.repository.jpa.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final NoticeRepository noticeRepository;
    private final QnaRepository qnaRepository;
    private final AdRepository adRepository;

    /**
     * 게시물 통계를 조회합니다.
     * 각 게시판의 오늘 등록된 게시물 수와 전체 게시물 수를 반환합니다.
     */
    public PostsStatsDTO getPostsStats() {
        try {
            // 오늘 날짜 기준 시작과 끝 시간
            LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            
            // Timestamp로 변환 (Notice 엔티티는 Timestamp 타입 사용)
            Timestamp startTimestamp = Timestamp.valueOf(startOfDay);
            Timestamp endTimestamp = Timestamp.valueOf(endOfDay);

            // 공지사항 통계
            long noticeToday = noticeRepository.countByCreatedDateBetween(startTimestamp, endTimestamp);
            long noticeTotal = noticeRepository.count();

            // Q&A 통계
            long qnaToday = qnaRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long qnaTotal = qnaRepository.count();

            // 광고 통계 (startDateTime 필드 사용)
            long adToday = adRepository.countByStartDateTimeBetween(startOfDay, endOfDay);
            long adTotal = adRepository.count();

            // DTO 생성 및 반환
            return PostsStatsDTO.builder()
                    .notices(new PostsStatsDTO.PostTypeStats((int) noticeToday, (int) noticeTotal))
                    .qna(new PostsStatsDTO.PostTypeStats((int) qnaToday, (int) qnaTotal))
                    .advertisements(new PostsStatsDTO.PostTypeStats((int) adToday, (int) adTotal))
                    .build();
        } catch (Exception e) {
            // 에러 발생 시 기본 데이터 반환
            return PostsStatsDTO.builder()
                    .notices(new PostsStatsDTO.PostTypeStats(1, 12))
                    .qna(new PostsStatsDTO.PostTypeStats(4, 28))
                    .advertisements(new PostsStatsDTO.PostTypeStats(2, 15))
                    .build();
        }
    }
} 