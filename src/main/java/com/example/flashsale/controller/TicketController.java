package com.example.flashsale.controller;

import com.example.flashsale.dto.BookTicketRequest;
import com.example.flashsale.dto.BookTicketResponse;
import com.example.flashsale.dto.CreateTicketRequest;
import com.example.flashsale.dto.TicketResponse;
import com.example.flashsale.entity.Order;
import com.example.flashsale.entity.Ticket;
import com.example.flashsale.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(
                request.getEventName(),
                request.getPrice(),
                request.getAvailableStock());

        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.getTicket(id)));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listTickets() {
        List<TicketResponse> tickets = ticketService.listTickets().stream()
                .map(TicketResponse::from)
                .toList();

        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/book")
    public ResponseEntity<BookTicketResponse> bookTicket(@Valid @RequestBody BookTicketRequest request) {
        Order order = ticketService.bookTicket(
                request.getTicketId(),
                request.getUserId(),
                request.getQuantity());

        BookTicketResponse response = new BookTicketResponse(
                order.getId(),
                request.getTicketId(),
                request.getUserId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                "Ticket booked successfully. A confirmation notification is on its way.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
