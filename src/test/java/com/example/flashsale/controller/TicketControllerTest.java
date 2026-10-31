package com.example.flashsale.controller;

import com.example.flashsale.entity.Ticket;
import com.example.flashsale.entity.User;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flashsale;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void bookingReturnsCreatedResponse() throws Exception {
        User user = userRepository.save(new User("Controller User", "controller@example.com"));
        Ticket ticket = ticketRepository.save(
                new Ticket("Spring Boot Live", new BigDecimal("99.00"), 5));

        String body = """
                {"ticketId": %d, "userId": %d, "quantity": 2}
                """.formatted(ticket.getId(), user.getId());

        mockMvc.perform(post("/api/v1/tickets/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.ticketId").value(ticket.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(198.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(3, updatedTicket.getAvailableStock());
    }

    @Test
    void bookingReturnsConflictWhenOutOfStock() throws Exception {
        User user = userRepository.save(new User("Out Of Stock", "oos@example.com"));
        Ticket ticket = ticketRepository.save(
                new Ticket("Sold Out Show", new BigDecimal("50.00"), 1));

        String body = """
                {"ticketId": %d, "userId": %d, "quantity": 2}
                """.formatted(ticket.getId(), user.getId());

        mockMvc.perform(post("/api/v1/tickets/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        Ticket unchangedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(1, unchangedTicket.getAvailableStock());
    }

    @Test
    void bookingReturnsNotFoundForUnknownTicket() throws Exception {
        User user = userRepository.save(new User("Missing Ticket", "missing@example.com"));

        String body = """
                {"ticketId": 99999, "userId": %d, "quantity": 1}
                """.formatted(user.getId());

        mockMvc.perform(post("/api/v1/tickets/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void bookingReturnsBadRequestForInvalidQuantity() throws Exception {
        User user = userRepository.save(new User("Invalid Quantity", "invalid@example.com"));
        Ticket ticket = ticketRepository.save(
                new Ticket("Validation Show", new BigDecimal("20.00"), 5));

        String body = """
                {"ticketId": %d, "userId": %d, "quantity": 0}
                """.formatted(ticket.getId(), user.getId());

        mockMvc.perform(post("/api/v1/tickets/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("quantity must be greater than zero"));
    }
}
