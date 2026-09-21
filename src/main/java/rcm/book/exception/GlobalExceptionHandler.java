package rcm.book.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rcm.book.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Translates exceptions into the single {@link ErrorResponseDTO} contract.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResource(DuplicateResourceException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex,
                                                                 HttpServletRequest request) {
        // Deliberately generic: never reveal whether the username or the password was wrong.
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password.", request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalService(ExternalServiceException ex,
                                                                 HttpServletRequest request) {
        log.warn("Upstream dependency failed for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /** Validation failures on {@code @RequestBody} DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /** Validation failures on method parameters such as {@code @RequestParam} and {@code @PathVariable}. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> lastPathNode(violation.getPropertyPath().toString()) + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Catch-all. Framework exceptions (unknown route, wrong HTTP method, unreadable body, ...)
     * implement {@link ErrorResponse} and already carry the correct status, so those are
     * re-rendered in our shape rather than masked as a 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            return build(errorResponse.getStatusCode(), ex.getMessage(), request);
        }
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatusCode status, String message, HttpServletRequest request) {
        HttpStatus resolved = HttpStatus.valueOf(status.value());
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                resolved.value(),
                resolved.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(resolved).body(body);
    }

    private String lastPathNode(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
