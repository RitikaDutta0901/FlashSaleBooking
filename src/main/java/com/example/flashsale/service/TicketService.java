package com.example.flashsale.service;

import com.example.flashsale.entity.Order;
import com.example.flashsale.entity.Ticket;
import com.example.flashsale.entity.User;
import com.example.flashsale.exception.OutOfStockException;
import com.example.flashsale.exception.ResourceNotFoundException;
import com.example.flashsale.repository.OrderRepository;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         OrderRepository orderRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order bookTicket(Long ticketId, Long userId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Ticket ticket = ticketRepository.findByIdWithPessimisticLock(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getAvailableStock() < quantity) {
            throw new OutOfStockException(
                    "Not enough tickets available for event: " + ticket.getEventName());
        }

        ticket.setAvailableStock(ticket.getAvailableStock() - quantity);
        ticketRepository.save(ticket);

        BigDecimal totalAmount = ticket.getPrice().multiply(BigDecimal.valueOf(quantity));
        Order order = new Order(user, ticket, quantity, totalAmount, "CONFIRMED");
        Order savedOrder = orderRepository.save(order);

        sendBookingNotification(savedOrder.getId(), user.getEmail(), ticket.getEventName());

        return savedOrder;
    }

    private void sendBookingNotification(Long orderId, String email, String eventName) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Mock notification sent: order {} confirmed for {} at {}",
                    orderId, email, eventName);
        });
    }
}
