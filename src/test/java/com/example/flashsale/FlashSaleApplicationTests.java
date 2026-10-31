package com.example.flashsale;

import com.example.flashsale.entity.Ticket;
import com.example.flashsale.repository.OrderRepository;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flashsale;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class FlashSaleApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void contextLoadsWithAllRepositoryBeans() {
        assertNotNull(userRepository);
        assertNotNull(ticketRepository);
        assertNotNull(orderRepository);
    }

    @Test
    void lockedQueryReturnsTicket() {
        Ticket ticket = ticketRepository.save(
                new Ticket("Java Conference", new BigDecimal("99.00"), 10));

        Optional<Ticket> lockedTicket = ticketRepository.findByIdWithPessimisticLock(ticket.getId());

        assertTrue(lockedTicket.isPresent());
        assertEquals(10, lockedTicket.get().getAvailableStock());
    }
}
