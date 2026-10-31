package com.example.flashsale.dto;

import com.example.flashsale.entity.Ticket;

import java.math.BigDecimal;

public class TicketResponse {

    private Long id;
    private String eventName;
    private BigDecimal price;
    private int availableStock;

    public TicketResponse(Long id, String eventName, BigDecimal price, int availableStock) {
        this.id = id;
        this.eventName = eventName;
        this.price = price;
        this.availableStock = availableStock;
    }

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getEventName(),
                ticket.getPrice(),
                ticket.getAvailableStock());
    }

    public Long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
