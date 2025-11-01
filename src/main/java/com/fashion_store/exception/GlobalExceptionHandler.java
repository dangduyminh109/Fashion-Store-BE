package com.fashion_store.exception;

import com.fashion_store.dto.common.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.ParseException;
import java.util.Arrays;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> ExceptionHandler(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_EXCEPTION;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_FORM_FORMAT;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();

            if ("typeMismatch".equals(fieldError.getCode())
                    || (fieldError.getCodes() != null && Arrays.stream(fieldError.getCodes()).anyMatch(code -> code.contains("typeMismatch")))) {
                if (fieldName.contains("promotionStartTime")) {
                    errorCode = ErrorCode.INVALID_PROMOTION_START_TIME;
                } else if (fieldName.contains("promotionEndTime")) {
                    errorCode = ErrorCode.INVALID_PROMOTION_END_TIME;
                } else if (fieldName.contains("variantList")) {
                    errorCode = ErrorCode.INVALID_VARIANT_LIST;
                } else if (fieldName.contains("image")) {
                    errorCode = ErrorCode.INVALID_FILE;
                } else {
                    errorCode = ErrorCode.INVALID_TYPE_DATA;
                }
            } else {
                try {
                    errorCode = ErrorCode.valueOf(fieldError.getDefaultMessage());
                } catch (IllegalArgumentException ex) {
                    log.error("ErrorCode {} does not exist in enum\n{}", fieldError.getDefaultMessage(), ex.getMessage());
                    errorCode = ErrorCode.INVALID_KEY;
                }
            }
            break;
        }

        if (e.getBindingResult().getFieldErrors().isEmpty() && !e.getBindingResult().getGlobalErrors().isEmpty()) {
            String defaultMessage = e.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
            try {
                errorCode = ErrorCode.valueOf(defaultMessage);
            } catch (IllegalArgumentException ex) {
                log.error("Global ErrorCode {} does not exist in enum\n{}", defaultMessage, ex.getMessage());
                errorCode = ErrorCode.INVALID_KEY;
            }
        }

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> AppExceptionHandler(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // token hết hạn
    @ExceptionHandler(value = AuthenticationServiceException.class)
    ResponseEntity<ApiResponse> AuthenticationServiceExceptionHandler(AuthenticationServiceException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // lỗi content-type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> HttpMediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // không có quyền truy cập
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> AccessDeniedExceptionHandler(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // không có quyền truy cập
    @ExceptionHandler(value = JwtException.class)
    ResponseEntity<ApiResponse> JwtExceptionHandler(JwtException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // lỗi giải token
    @ExceptionHandler(ParseException.class)
    public ResponseEntity<ApiResponse<Void>> ParseExceptionHandler(ParseException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // sai key hoặt thuật toán
    @ExceptionHandler(JOSEException.class)
    public ResponseEntity<ApiResponse<Void>> JOSEExceptionHandler(JOSEException ex) {
        ErrorCode errorCode = ErrorCode.CANNOT_CREATE_TOKEN;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // lỗi không gửi được mail
    @ExceptionHandler(MessagingException.class)
    ResponseEntity<ApiResponse<Void>> MessagingExceptionHandler(MessagingException e) {
        ErrorCode errorCode = ErrorCode.EMAIL_SEND_FAILED;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }
}
