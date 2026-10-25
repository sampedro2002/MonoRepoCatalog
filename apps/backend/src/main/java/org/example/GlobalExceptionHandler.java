package org.example;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps every error to an RFC 7807 {@link ProblemDetail} that carries a {@code traceId}.
 * The same id is returned in the {@code X-Trace-Id} response header and written to the log,
 * so a client-reported error can be matched to its server-side log line.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TRACE_ID_HEADER = "X-Trace-Id";
    static final String TRACE_ID_PROPERTY = "traceId";

    // Accepts only short, plain ids from the caller, so arbitrary input never reaches the logs
    private static final Pattern VALID_TRACE_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        // Headers passed by Spring can be read-only, so copy them before adding the trace id
        HttpHeaders writableHeaders = new HttpHeaders();
        writableHeaders.putAll(headers);
        writableHeaders.set(TRACE_ID_HEADER, traceId);

        // The body is often null here and built by the superclass, so add the trace id after it exists
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, writableHeaders, statusCode, request);
        if (response.getBody() instanceof ProblemDetail problem) {
            problem.setProperty(TRACE_ID_PROPERTY, traceId);
        }
        log.warn("traceId={} status={} error={}", traceId, statusCode.value(), ex.getMessage());
        return response;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        log.error("traceId={} unexpected error", traceId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal Server Error");
        problem.setProperty(TRACE_ID_PROPERTY, traceId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(TRACE_ID_HEADER, traceId)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private static String resolveTraceId(@Nullable String incoming) {
        if (incoming != null && VALID_TRACE_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}
