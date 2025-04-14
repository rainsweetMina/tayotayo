package kroryi.bus2.service;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.repository.jpa.NoticeRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {


    private final NoticeRepository noticeRepository;

    // 공지 등록
    @Override
    public NoticeResponseDTO createNotice(CreateNoticeRequestDTO dto) {
        Notice entity = new Notice();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setAuthor(dto.getAuthor());

        noticeRepository.save(entity); // ← 이게 있어야 진짜 저장됨
        return new NoticeResponseDTO(entity); // 저장된 결과를 다시 DTO로 반환
    }


    // 공지 수정
    @Override
    public NoticeResponseDTO updateNotice(Long id, UpdateNoticeRequestDTO dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지 없음"));

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        Notice updated = noticeRepository.save(notice);
        return new NoticeResponseDTO(updated);
    }

    // 공지 삭제
    @Override
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 존재하지 않습니다."));

        noticeRepository.delete(notice);
    }

    // 공지 전체 목록
    @Override
    public List<NoticeResponseDTO> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(NoticeResponseDTO::new)
                .collect(Collectors.toList());
    }


    @Override
    // 공지 상세 조회
    public NoticeResponseDTO getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항 없음"));
        return new NoticeResponseDTO(notice);
    }

}
