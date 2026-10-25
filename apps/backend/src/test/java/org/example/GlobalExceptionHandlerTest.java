package org.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(HealthController.class)
@Import(GlobalExceptionHandlerTest.FailingController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validIncomingTraceIdIsPropagated() throws Exception {
        mockMvc.perform(get("/api/fail").header(GlobalExceptionHandler.TRACE_ID_HEADER, "abc-123"))
                .andExpect(header().string(GlobalExceptionHandler.TRACE_ID_HEADER, "abc-123"))
                .andExpect(jsonPath("$.traceId").value("abc-123"));
    }

    @Test
    void invalidIncomingTraceIdIsReplaced() throws Exception {
        mockMvc.perform(get("/api/fail").header(GlobalExceptionHandler.TRACE_ID_HEADER, "bad id\n<script>"))
                .andExpect(jsonPath("$.traceId").value(Matchers.matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    void unexpectedExceptionReturnsGenericProblemWithTraceId() throws Exception {
        mockMvc.perform(get("/api/fail").header(GlobalExceptionHandler.TRACE_ID_HEADER, "fail-1"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(GlobalExceptionHandler.TRACE_ID_HEADER, "fail-1"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.traceId").value("fail-1"));
    }

    @RestController
    static class FailingController {

        @GetMapping("/api/fail")
        String fail() {
            throw new IllegalStateException("internal detail that must not leak");
        }
    }
}
