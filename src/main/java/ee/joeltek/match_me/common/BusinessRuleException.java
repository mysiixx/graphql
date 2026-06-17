package ee.joeltek.match_me.common;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends RuntimeException {
    private final HttpStatus status;

    
    public BusinessRuleException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessRuleException(HttpStatus status, String message) {
    super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}