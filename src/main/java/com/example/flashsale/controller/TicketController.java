package com.example.flashsale.controller;

import com.example.flashsale.dto.BookTicketRequest;
import com.example.flashsale.dto.BookTicketResponse;
import com.example.flashsale.entity.Order;
import com.example.flashsale.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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
