package com.example.flashsale.service;

import com.example.flashsale.entity.Order;
import com.example.flashsale.entity.Ticket;
import com.example.flashsale.entity.User;
import com.example.flashsale.exception.OutOfStockException;
import com.example.flashsale.repository.OrderRepository;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flashsale;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void bookingDecrementsStockAndCreatesOrder() {
        User user = userRepository.save(new User("Ritika", "ritika@example.com"));
        Ticket ticket = ticketRepository.save(
                new Ticket("Java Conference", new BigDecimal("99.00"), 5));

        Order order = ticketService.bookTicket(ticket.getId(), user.getId(), 2);

        assertNotNull(order.getId());
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals(2, order.getQuantity());
        assertEquals(0, new BigDecimal("198.00").compareTo(order.getTotalAmount()));

        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(3, updatedTicket.getAvailableStock());

        assertEquals(1, orderRepository.findByUserId(user.getId()).size());
    }

    @Test
    void bookingFailsWhenStockIsNotEnough() {
        User user = userRepository.save(new User("Ritika", "ritika2@example.com"));
        Ticket ticket = ticketRepository.save(
                new Ticket("Java Conference", new BigDecimal("99.00"), 1));

        assertThrows(OutOfStockException.class,
                () -> ticketService.bookTicket(ticket.getId(), user.getId(), 2));

        Ticket unchangedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(1, unchangedTicket.getAvailableStock());
    }
}
