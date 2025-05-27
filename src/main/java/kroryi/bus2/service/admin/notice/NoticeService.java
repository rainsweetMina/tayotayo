package kroryi.bus2.service.admin.notice;

import kroryi.bus2.dto.notice.NoticeDTO;
import kroryi.bus2.entity.Notice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


public interface NoticeService {
    NoticeDTO createNotice(NoticeDTO dto, List<MultipartFile> files);
    NoticeDTO updateNotice(Long id, NoticeDTO dto, List<MultipartFile> files);
    void delete(Long id);
    NoticeDTO findById(Long id);
    List<NoticeDTO> findAll();
    List<NoticeDTO> getAllNotices();
    NoticeDTO getNoticeById(Long id);

    //팝업관련
    Optional<Notice> findValidPopup();


}
