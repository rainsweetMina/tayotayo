package kroryi.bus2.service.admin.notice;

import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.entity.Notice;

import java.util.List;
import java.util.Optional;

public interface NoticeService {
    NoticeResponseDTO createNotice(CreateNoticeRequestDTO dto);
    NoticeResponseDTO updateNotice(Long id, UpdateNoticeRequestDTO dto);
    void deleteNotice(Long id);
    List<NoticeResponseDTO> getAllNotices();
    NoticeResponseDTO getNoticeById(Long id);

    //팝업관련
    Optional<Notice> findValidPopup();
}
