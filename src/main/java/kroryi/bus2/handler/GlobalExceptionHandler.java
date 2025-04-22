package kroryi.bus2.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    // 400 - 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // 500 - 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다1.");
    }
}
