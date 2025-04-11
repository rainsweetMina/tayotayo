package kroryi.bus2.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error/common-error";
    }

//    @ExceptionHandler(Exception.class)
//    public String handleException(Exception e, Model model) {
////        log.error("Exception: {}", e.getMessage());
//        log.error("Exception: {}", e);
//        model.addAttribute("error", "오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
//        return "error/common-error";
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Exception occurred", e); // ✅ 전체 스택트레이스 포함
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
}


}
