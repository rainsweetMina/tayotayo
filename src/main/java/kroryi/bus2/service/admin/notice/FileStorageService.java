package kroryi.bus2.service.admin.notice;

import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    List<NoticeFile> storeFiles(List<MultipartFile> files, Notice notice);
}
