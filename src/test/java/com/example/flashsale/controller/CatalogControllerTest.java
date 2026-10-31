package com.example.flashsale.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUserReturnsCreatedResponse() throws Exception {
        String body = """
                {"name": "Catalog User", "email": "catalog@example.com"}
                """;

        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Catalog User"))
                .andExpect(jsonPath("$.email").value("catalog@example.com"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = """
                {"name": "Duplicate User", "email": "duplicate@example.com"}
                """;

        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createAndFetchTicket() throws Exception {
        String body = """
                {"eventName": "Catalog Concert", "price": 49.99, "availableStock": 25}
                """;

        String response = mockMvc.perform(post("/api/v1/tickets")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.eventName").value("Catalog Concert"))
                .andExpect(jsonPath("$.availableStock").value(25))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/v1/tickets/" + id)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("Catalog Concert"))
                .andExpect(jsonPath("$.availableStock").value(25));
    }

    @Test
    void createTicketWithoutAuthenticationReturnsUnauthorized() throws Exception {
        String body = """
                {"eventName": "No Auth", "price": 10.00, "availableStock": 1}
                """;

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
