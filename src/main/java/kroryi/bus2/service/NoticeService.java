package kroryi.bus2.service;

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

    public Notice addNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    public Notice updateNotice(Long id, Notice newNotice) {
        return noticeRepository.findById(id)
                .map(notice -> {
                    notice.setTitle(newNotice.getTitle());
                    notice.setContent(newNotice.getContent());
                    notice.setAuthor(newNotice.getAuthor());
                    return noticeRepository.save(notice);
                })
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedDateDesc();
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));
    }

}
