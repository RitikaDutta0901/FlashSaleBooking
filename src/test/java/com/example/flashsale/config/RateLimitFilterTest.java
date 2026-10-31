package com.example.flashsale.config;

import com.example.flashsale.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    @Test
    void blocksRequestsAfterLimitIsReached() throws Exception {
        RateLimitService rateLimitService = new RateLimitService(2, "memory", null);
        RateLimitFilter filter = new RateLimitFilter(
                rateLimitService, new ObjectMapper().findAndRegisterModules());

        assertEquals(200, execute(filter).getStatus());
        assertEquals(200, execute(filter).getStatus());

        MockHttpServletResponse blockedResponse = execute(filter);

        assertEquals(429, blockedResponse.getStatus());
        assertTrue(blockedResponse.getContentAsString().contains("Rate limit exceeded"));
    }

    private MockHttpServletResponse execute(RateLimitFilter filter) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tickets/book");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        return response;
    }
}
