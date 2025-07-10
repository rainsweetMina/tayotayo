package kroryi.bus2.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.NoHandlerFoundException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message, String errorType) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("error", errorType);
        return ResponseEntity.status(status).body(errorResponse);
    }

    // 400 - 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), "IllegalArgumentException");
    }

    // 파일 업로드 관련 예외
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException e) {
        log.error("MultipartException: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "파일 업로드 중 오류가 발생했습니다.", "MultipartException");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 허용된 최대 크기를 초과했습니다.", "MaxUploadSizeExceededException");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException e) {
        log.error("IOException: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 I/O 오류가 발생했습니다.", "IOException");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.FORBIDDEN, "파일 접근 권한이 없습니다.", "AccessDeniedException");
    }

    // 404 - 리소스를 찾을 수 없음
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException: {} {}", e.getHttpMethod(), e.getRequestURL());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", "NoResourceFoundException");
    }

    // 500 - 서버 오류
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException: ", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Null 포인터 예외가 발생했습니다.", "NullPointerException");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException e) {
        log.error("DataAccessException: ", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 접근 오류가 발생했습니다.", "DataAccessException");
    }

    // 모든 예외에 대한 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", e.getClass().getSimpleName());
    }
}
