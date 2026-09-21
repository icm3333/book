package rcm.book.exception;

/** Raised when a third-party service the API depends on fails or is unreachable. */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message){
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause){
        super(message, cause);
    }
}
