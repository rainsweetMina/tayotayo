package kroryi.bus2.service;


import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.entity.Notice;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Log4j2
public class NoticeTests {

    @Autowired
    private NoticeServiceImpl noticeService;

    @Test
    public void testGetAllNotices() {
        List<NoticeResponseDTO> notices = noticeService.getAllNotices();
        log.info("Notices: {}", notices);
        assertNotNull(notices);
    }


}
