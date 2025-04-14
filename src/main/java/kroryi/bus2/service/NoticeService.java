package kroryi.bus2.service;

import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;

import java.util.List;

public interface NoticeService {
    NoticeResponseDTO createNotice(CreateNoticeRequestDTO dto);
    NoticeResponseDTO updateNotice(Long id, UpdateNoticeRequestDTO dto);
    void deleteNotice(Long id);
    List<NoticeResponseDTO> getAllNotices();
    NoticeResponseDTO getNoticeById(Long id);
}
