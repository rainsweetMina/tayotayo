package kroryi.bus2.service.admin.notice;

import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.entity.Notice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


public interface NoticeService {
    NoticeResponseDTO createNotice(CreateNoticeRequestDTO dto, List<MultipartFile> files);
    NoticeResponseDTO updateNotice(Long id, UpdateNoticeRequestDTO dto, List<MultipartFile> files);
    void deleteNotice(Long id);
    List<NoticeResponseDTO> getAllNotices();
    NoticeResponseDTO getNoticeById(Long id);

    //팝업관련
    Optional<Notice> findValidPopup();
}
