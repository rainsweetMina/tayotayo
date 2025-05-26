package kroryi.bus2.service.admin.notice;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.dto.notice.NoticeDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import kroryi.bus2.repository.jpa.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileStorageService fileStorageService;

    @Override
    @AdminAudit(action = "공지 등록", target = "공지사항")
    public NoticeDTO createNotice(NoticeDTO dto, List<MultipartFile> files) {
        Notice notice = new Notice();
        populateNoticeFields(dto, notice);
        notice.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        noticeRepository.save(notice);

        if (files != null && !files.isEmpty()) {
            List<NoticeFile> storedFiles = fileStorageService.storeFiles(files, notice);
            notice.updateFiles(storedFiles);
        }

        return new NoticeDTO(notice);
    }

    @Override
    @AdminAudit(action = "공지 수정", target = "공지사항")
    public NoticeDTO updateNotice(Long id, NoticeDTO dto, List<MultipartFile> files) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지 없음"));

        populateNoticeFields(dto, notice);
        Notice savedNotice = noticeRepository.save(notice);
        return new NoticeDTO(savedNotice);
    }

    @Override
    @AdminAudit(action = "공지 삭제", target = "공지사항")
    public void delete(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 공지사항이 존재하지 않습니다."));
        noticeRepository.delete(notice);
    }

    @Override
    public NoticeDTO findById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다: " + id));
        return new NoticeDTO(notice);
    }

    @Override
    public List<NoticeDTO> findAll() {
        return noticeRepository.findAll().stream()
                .map(notice -> new NoticeDTO(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getAuthor(),
                        notice.getContent(),
                        notice.getCreatedDate().toLocalDateTime(),
                        notice.getUpdatedDate() != null ? notice.getUpdatedDate().toLocalDateTime() : null, // Handle null updatedDate
                        notice.isShowPopup(),
                        notice.getPopupStart(),
                        notice.getPopupEnd(),
                        notice.getFiles().stream()
                                .map(NoticeDTO.FileDTO::from)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }


    @Override
    public List<NoticeDTO> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(NoticeDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public NoticeDTO getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항 없음"));
        return new NoticeDTO(notice);
    }

    @AdminTracked
    @Override
    public Optional<Notice> findValidPopup() {
        LocalDateTime now = LocalDateTime.now();
        return noticeRepository.findFirstByShowPopupTrueAndPopupStartBeforeAndPopupEndAfterOrderByPopupStartDesc(now, now);
    }

    // Extracted method: Populates fields of Notice entity
    private void populateNoticeFields(NoticeDTO dto, Notice notice) {
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setAuthor(dto.getAuthor());
        notice.setShowPopup(dto.isShowPopup());
        notice.setPopupStart(dto.getPopupStart());
        notice.setPopupEnd(dto.getPopupEnd());
        notice.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
    }
}