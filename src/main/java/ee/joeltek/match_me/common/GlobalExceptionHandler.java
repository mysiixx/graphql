package ee.joeltek.match_me.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import ee.joeltek.match_me.connection.ConnectionStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ProblemDetailFactory problemFactory;
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        String detail = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason()
                : "The request failed";

        ProblemDetail problem = problemFactory.create(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                "Request Failed",
                detail,
                request.getRequestURI()
        );
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.toList()
                        )
                ));
        ProblemDetail problem = problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more fields are invalid.",
                request.getRequestURI()
        );
        if (!fieldErrors.isEmpty()) problem.setProperty("errors", fieldErrors);

        return problem;
    }

    @ExceptionHandler(ResourceExistsException.class)
    public ProblemDetail handleResourceExistsException(
        ResourceExistsException ex,
        HttpServletRequest request
    ) {
        return problemFactory.create(
            HttpStatus.CONFLICT,
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(
        ResourceNotFoundException ex,
        HttpServletRequest request
    ) {
        return problemFactory.create(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ProblemDetail handleInvalidOperationException(
        InvalidOperationException ex,
        HttpServletRequest request
    ) {
        return problemFactory.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Invalid Operation",
            request.getRequestURI(),
            ex.getMessage()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = problemFactory.create(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                "HTTP method '" + request.getMethod() + "' is not supported for this endpoint.",
                request.getRequestURI()
        );

        problem.setProperty("allowedMethods", ex.getSupportedMethods());

        return problem;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRuleException(
        BusinessRuleException ex,
        HttpServletRequest request
    ) {
        return problemFactory.create(
            ex.getStatus(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    // Handles @Valid annotations
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidationException(
        HandlerMethodValidationException ex,
        HttpServletRequest request
    ) {
        Map<String, Object> errors = ex.getParameterValidationResults()
            .stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> Map.entry(
                    result.getMethodParameter().getParameterName(),
                    error.getDefaultMessage()
                )))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        return problemFactory.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Validation Failure",
            request.getRequestURI(),
            errors
        );
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    ProblemDetail handleUnauthorizedOperationException(
        UnauthorizedOperationException ex,
        HttpServletRequest request
    ) {
        return problemFactory.create(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable cause = ex.getMostSpecificCause();

        // Handles invalid fields in JSON body
        if(cause instanceof InvalidFormatException ife) {
            if(ife.getTargetType().isEnum()) {
                List<String> acceptedValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .filter(val -> !val.equals(ConnectionStatus.PENDING.toString()))
                    .toList();

                return problemFactory.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "Request body is missing or malformed",
                    request.getRequestURI(),
                    "Invalid value '" + ife.getValue() + "'. Accepted values are: " + acceptedValues
                );
            }
        }

        // Fallback
        return problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Request body is missing or malformed",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage() == null || ex.getMessage().isBlank() ? "Request body is missing or malformed" : ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return problemFactory.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "Requested resource was not found",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllOtherExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception for request {}", request.getRequestURI(), ex);

        return problemFactory.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI()
        );

    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleMultipartException(
            MultipartException ex,
            HttpServletRequest request) {

        return problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "INVALID_MULTIPART_REQUEST",
                "Uploaded file could not be processed. Please select the file again and retry.",
                request.getRequestURI()
        );
    }
}