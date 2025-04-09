package kroryi.bus2.service;


import kroryi.bus2.entity.Notice;
import kroryi.bus2.repository.jpa.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@SpringBootTest
@Log4j2
public class NoticeTests {

    @Autowired
    private NoticeService noticeService;

    @Test
    public void testGetAllNotices() {
        List<Notice> notices = noticeService.getAllNotices();
        log.info("Notices: {}", notices);
    }


}
