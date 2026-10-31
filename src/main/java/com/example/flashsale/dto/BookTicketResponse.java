package com.example.flashsale.dto;

import java.math.BigDecimal;

public class BookTicketResponse {

    private Long orderId;
    private Long ticketId;
    private Long userId;
    private int quantity;
    private BigDecimal totalAmount;
    private String status;
    private String message;

    public BookTicketResponse(Long orderId, Long ticketId, Long userId, int quantity,
                              BigDecimal totalAmount, String status, String message) {
        this.orderId = orderId;
        this.ticketId = ticketId;
        this.userId = userId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getUserId() {
        return userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
