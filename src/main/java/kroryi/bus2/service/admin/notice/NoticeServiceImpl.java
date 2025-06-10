package kroryi.bus2.service.admin.notice;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import kroryi.bus2.repository.jpa.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    @AdminAudit(action = "공지 등록", target = "공지사항")
    public NoticeResponseDTO createNotice(CreateNoticeRequestDTO dto, List<MultipartFile> files) {
        try {
            log.info("공지사항 생성 시작 - 제목: {}", dto.getTitle());
            
            // 1. 기본 정보로 엔티티 생성
            Notice entity = new Notice();
            entity.setTitle(dto.getTitle());
            entity.setContent(dto.getContent());
            entity.setAuthor(dto.getAuthor());
            entity.setShowPopup(dto.isShowPopup());
            entity.setPopupStart(dto.getPopupStart());
            entity.setPopupEnd(dto.getPopupEnd());

            // 2. 기본 정보 저장
            log.info("공지사항 기본 정보 저장");
            Notice savedNotice = noticeRepository.save(entity);
            log.info("공지사항 저장 완료 - ID: {}", savedNotice.getId());

            // 3. 파일 처리 및 연결
            if (files != null && !files.isEmpty()) {
                log.info("첨부파일 처리 시작 - 파일 수: {}", files.size());
                List<NoticeFile> storedFiles = fileStorageService.storeFiles(files, savedNotice);
                log.info("첨부파일 저장 완료 - 저장된 파일 수: {}", storedFiles.size());
                
                if (!storedFiles.isEmpty()) {
                    savedNotice.updateFiles(storedFiles);
                    savedNotice = noticeRepository.save(savedNotice);
                    log.info("첨부파일 정보가 포함된 공지사항 저장 완료");
                }
            }

            return new NoticeResponseDTO(savedNotice);
        } catch (Exception e) {
            log.error("공지사항 생성 중 오류 발생", e);
            throw new RuntimeException("공지사항 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 공지 수정
    @Transactional
    @AdminAudit(action = "공지 수정", target = "공지사항")
    @Override
    public NoticeResponseDTO updateNotice(Long id, UpdateNoticeRequestDTO dto, List<MultipartFile> files) {
        try {
            log.info("공지사항 수정 시작 - ID: {}, 제목: {}", id, dto.getTitle());
            
            Notice notice = noticeRepository.findByIdWithFiles(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 공지 없음"));

            notice.setTitle(dto.getTitle());
            notice.setContent(dto.getContent());
            notice.setShowPopup(dto.isShowPopup());
            notice.setPopupStart(dto.getPopupStart());
            notice.setPopupEnd(dto.getPopupEnd());

            // 파일 처리
            if (files != null && !files.isEmpty()) {
                log.info("첨부파일 처리 시작 - 파일 수: {}", files.size());
                List<NoticeFile> storedFiles = fileStorageService.storeFiles(files, notice);
                log.info("첨부파일 저장 완료 - 저장된 파일 수: {}", storedFiles.size());
                
                if (!storedFiles.isEmpty()) {
                    notice.updateFiles(storedFiles);
                }
            }

            // 변경사항 저장
            Notice updated = noticeRepository.save(notice);
            log.info("공지사항 수정 완료 - ID: {}", updated.getId());
            
            return new NoticeResponseDTO(updated);
        } catch (Exception e) {
            log.error("공지사항 수정 중 오류 발생", e);
            throw new RuntimeException("공지사항 수정 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 공지 삭제
    @Transactional
    @AdminAudit(action = "공지 삭제", target = "공지사항")
    @Override
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 존재하지 않습니다."));

        noticeRepository.delete(notice);
    }

    // 공지 전체 목록
    @Transactional(readOnly = true)
    @Override
    public List<NoticeResponseDTO> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(NoticeResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 공지 상세 조회
    @Transactional
    @Override
    public NoticeResponseDTO getNoticeById(Long id) {
        Notice notice = noticeRepository.findByIdWithFiles(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항 없음"));
        
        // 조회수 증가
        notice.increaseViewCount();
        notice = noticeRepository.save(notice);
        
        return new NoticeResponseDTO(notice);
    }
    
    @Transactional(readOnly = true)
    @Override
    public Notice findById(Long id) {
        return noticeRepository.findByIdWithFiles(id).orElse(null);
    }

    //팝업관련
    @Transactional(readOnly = true)
    @AdminTracked
    @Override
    public Optional<Notice> findValidPopup() {
        LocalDateTime now = LocalDateTime.now();
        return noticeRepository.findFirstByShowPopupTrueAndPopupStartBeforeAndPopupEndAfterOrderByPopupStartDesc(now, now);
    }
}
