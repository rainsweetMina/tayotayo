package kroryi.bus2.service;

import kroryi.bus2.dto.NoticeDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.repository.jpa.NoticeRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;


    public NoticeDTO addNotice(NoticeDTO dto) {
        Notice entity = new Notice();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setAuthor(dto.getAuthor());

        noticeRepository.save(entity); // ← 이게 있어야 진짜 저장됨

        return new NoticeDTO(entity); // 저장된 결과를 다시 DTO로 반환
    }


    public Notice updateNotice(Long id, NoticeDTO dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지 없음"));

        notice.setTitle(dto.getTitle());
        notice.setAuthor(dto.getAuthor());
        notice.setContent(dto.getContent());

        return noticeRepository.save(notice);
    }


    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedDateDesc();
    }

    public List<NoticeDTO> getAllNoticeDTOs() {
        return noticeRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(notice -> new NoticeDTO(notice.getTitle(), notice.getAuthor(), notice.getContent()))
                .toList();
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));
    }



}
